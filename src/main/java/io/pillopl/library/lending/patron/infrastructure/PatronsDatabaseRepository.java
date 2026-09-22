package io.pillopl.library.lending.patron.infrastructure;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.EmailAddress;
import io.pillopl.library.lending.patron.model.EmailAddressAlreadyRegistered;
import io.pillopl.library.lending.patron.model.Patron;
import io.pillopl.library.lending.patron.model.PatronEvent;
import io.pillopl.library.lending.patron.model.PatronEvent.PatronCreated;
import io.pillopl.library.lending.patron.model.PatronFactory;
import io.pillopl.library.lending.patron.model.PatronHoldSnapshot;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.PatronStatus;
import io.pillopl.library.lending.patron.model.Patrons;

class PatronsDatabaseRepository implements Patrons {

  private final PatronEntityRepository patronEntityRepository;
  private final DomainModelMapper domainModelMapper;
  private final DomainEvents domainEvents;

  PatronsDatabaseRepository(
      PatronEntityRepository patronEntityRepository,
      DomainModelMapper domainModelMapper,
      DomainEvents domainEvents) {
    this.patronEntityRepository = patronEntityRepository;
    this.domainModelMapper = domainModelMapper;
    this.domainEvents = domainEvents;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Patron> findBy(PatronId patronId) {
    return patronEntityRepository
        .findByPatronId(patronId.getPatronId())
        .map(domainModelMapper::map);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean existsBy(EmailAddress emailAddress) {
    return patronEntityRepository.existsByEmailAddress(emailAddress.value());
  }

  @Override
  @Transactional
  public Patron publish(PatronEvent domainEvent) {
    Patron result =
        domainEvent instanceof PatronCreated patronCreated
            ? createNewPatron(patronCreated)
            : handleNextEvent(domainEvent);
    domainEvents.publish(domainEvent.normalize());
    return result;
  }

  private Patron createNewPatron(PatronCreated domainEvent) {
    try {
      PatronDatabaseEntity entity =
          patronEntityRepository.saveAndFlush(
              new PatronDatabaseEntity(
                  domainEvent.patronId(),
                  domainEvent.getPatronType(),
                  domainEvent.getEmailAddress()));
      return domainModelMapper.map(entity);
    } catch (DataIntegrityViolationException exception) {
      throw new EmailAddressAlreadyRegistered(domainEvent.getEmailAddress());
    }
  }

  private Patron handleNextEvent(PatronEvent domainEvent) {
    PatronDatabaseEntity entity =
        patronEntityRepository
            .findByPatronId(domainEvent.patronId().getPatronId())
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Patron not found: " + domainEvent.patronId().getPatronId()));
    entity.handle(domainEvent);
    patronEntityRepository.flush();
    return domainModelMapper.map(entity);
  }
}

interface PatronEntityRepository extends JpaRepository<PatronDatabaseEntity, Long> {

  Optional<PatronDatabaseEntity> findByPatronId(UUID patronId);

  boolean existsByEmailAddress(String emailAddress);
}

class DomainModelMapper {

  private final PatronFactory patronFactory;

  DomainModelMapper(PatronFactory patronFactory) {
    this.patronFactory = patronFactory;
  }

  Patron map(PatronDatabaseEntity entity) {
    return patronFactory.create(
        entity.patronType,
        new PatronId(entity.patronId),
        EmailAddress.of(entity.emailAddress),
        mapPatronHolds(entity),
        mapPatronOverdueCheckouts(entity),
        PatronStatus.valueOf(entity.status),
        entity.suspensionReason);
  }

  Map<LibraryBranchId, Set<BookId>> mapPatronOverdueCheckouts(
      PatronDatabaseEntity patronDatabaseEntity) {
    return patronDatabaseEntity.checkouts.stream()
        .collect(groupingBy(OverdueCheckoutDatabaseEntity::getLibraryBranchId, toSet()))
        .entrySet()
        .stream()
        .collect(
            toMap(
                (Entry<UUID, Set<OverdueCheckoutDatabaseEntity>> entry) ->
                    new LibraryBranchId(entry.getKey()),
                entry ->
                    entry.getValue().stream()
                        .map(entity -> new BookId(entity.bookId))
                        .collect(toSet())));
  }

  Set<PatronHoldSnapshot> mapPatronHolds(PatronDatabaseEntity patronDatabaseEntity) {
    return patronDatabaseEntity.booksOnHold.stream()
        .map(
            entity ->
                new PatronHoldSnapshot(
                    new BookId(entity.bookId),
                    new LibraryBranchId(entity.libraryBranchId),
                    entity.till,
                    entity.extensionCount))
        .collect(toSet());
  }
}
