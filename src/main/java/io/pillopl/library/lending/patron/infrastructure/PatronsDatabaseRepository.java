package io.pillopl.library.lending.patron.infrastructure;

import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.*;
import io.pillopl.library.lending.patron.model.PatronEvent.PatronCreated;
import io.vavr.Tuple;
import io.vavr.Tuple4;
import io.vavr.control.Option;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.dao.DuplicateKeyException;

import java.time.Instant;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;
import static java.util.stream.Collectors.*;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
class PatronsDatabaseRepository implements Patrons {

    private final PatronEntityRepository patronEntityRepository;
    private final DomainModelMapper domainModelMapper;
    private final DomainEvents domainEvents;

    @Override
    public Option<Patron> findBy(PatronId patronId) {
        return Option.of(patronEntityRepository
                .findByPatronId(patronId.getPatronId()))
                .map(domainModelMapper::map);
    }

    @Override
    public boolean existsBy(EmailAddress emailAddress){
        return patronEntityRepository.existsByEmailAddress(emailAddress.value());
    }

    @Override
    public Patron publish(PatronEvent domainEvent) {
        Patron result = Match(domainEvent).of(
                Case($(instanceOf(PatronCreated.class)), this::createNewPatron),
                Case($(), this::handleNextEvent));
        domainEvents.publish(domainEvent.normalize());
        return result;
    }

    private Patron createNewPatron(PatronCreated domainEvent) {
        try {
            PatronDatabaseEntity entity = patronEntityRepository
                    .save(new PatronDatabaseEntity(
                            domainEvent.patronId(),
                            domainEvent.getPatronType(),
                            domainEvent.getEmailAddress()));
            return domainModelMapper.map(entity);
        } catch (DuplicateKeyException exception) {
            throw new EmailAddressAlreadyRegistered(domainEvent.getEmailAddress());
        }
    }                      
        
    private Patron handleNextEvent(PatronEvent domainEvent) {
        PatronDatabaseEntity entity = patronEntityRepository.findByPatronId(domainEvent.patronId().getPatronId());
        entity = entity.handle(domainEvent);
        entity = patronEntityRepository.save(entity);
        return domainModelMapper.map(entity);
    }

}

interface PatronEntityRepository extends CrudRepository<PatronDatabaseEntity, Long> {

    @Query("SELECT p.* FROM patron_database_entity p where p.patron_id = :patronId")
    PatronDatabaseEntity findByPatronId(@Param("patronId") UUID patronId);

    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END " +
        "FROM patron_database_entity WHERE email_address = :emailAddress")
    boolean existsByEmailAddress(@Param("emailAddress") String emailAddress);
}

@AllArgsConstructor
class DomainModelMapper {

    private final PatronFactory patronFactory;

    Patron map(PatronDatabaseEntity entity) {
        return patronFactory.create(
                entity.patronType,
                new PatronId(entity.patronId),
                EmailAddress.of(entity.emailAddress),
                mapPatronHolds(entity),
                mapPatronOverdueCheckouts(entity),
                PatronStatus.valueOf(entity.status),
                entity.suspensionReason
        );
    }

    Map<LibraryBranchId, Set<BookId>> mapPatronOverdueCheckouts(PatronDatabaseEntity patronDatabaseEntity) {
        return
                patronDatabaseEntity
                        .checkouts
                        .stream()
                        .collect(groupingBy(OverdueCheckoutDatabaseEntity::getLibraryBranchId, toSet()))
                        .entrySet()
                        .stream()
                        .collect(toMap(
                                (Entry<UUID, Set<OverdueCheckoutDatabaseEntity>> entry) -> new LibraryBranchId(entry.getKey()), entry -> entry
                                        .getValue()
                                        .stream()
                                        .map(entity -> (new BookId(entity.bookId)))
                                        .collect(toSet())));
    }

    Set<Tuple4<BookId, LibraryBranchId, Instant, Integer>> mapPatronHolds(PatronDatabaseEntity patronDatabaseEntity) {
        return patronDatabaseEntity
                .booksOnHold
                .stream()
                .map(entity -> Tuple.of(
                        new BookId(entity.bookId),
                        new LibraryBranchId(entity.libraryBranchId),
                        entity.till,
                        entity.extensionCount))
                .collect(toSet());
    }

}
