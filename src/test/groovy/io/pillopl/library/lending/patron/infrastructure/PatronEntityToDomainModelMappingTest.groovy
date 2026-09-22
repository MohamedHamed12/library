package io.pillopl.library.lending.patron.infrastructure

import io.pillopl.library.catalogue.BookId
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.EmailAddress
import io.pillopl.library.lending.patron.model.Patron
import io.pillopl.library.lending.patron.model.PatronFactory
import io.pillopl.library.lending.patron.model.PatronHoldSnapshot
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.patron.model.PatronStatus
import io.pillopl.library.lending.patron.model.PatronType
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.book.model.BookFixture.circulatingAvailableBookAt
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.HoldDuration.closeEnded
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId
import static io.pillopl.library.lending.patron.model.PatronType.Regular

class PatronEntityToDomainModelMappingTest extends Specification {

    DomainModelMapper domainModelMapper = new DomainModelMapper(new PatronFactory())

    LibraryBranchId libraryBranchId = anyBranch()
    LibraryBranchId anotherBranchId = anyBranch()
    PatronId patronId = anyPatronId()
    BookId bookId = anyBookId()
    BookId anotherBookId = anyBookId()
    Instant anyDate = Instant.parse('2026-07-21T10:15:30Z')

    def 'should map patron holds including expiration and extension count'() {
        given:
            PatronDatabaseEntity entity = patronEntity(patronId, Regular)
            HoldDatabaseEntity first =
                    new HoldDatabaseEntity(
                            entity,
                            bookId.bookId,
                            patronId.patronId,
                            libraryBranchId.libraryBranchId,
                            anyDate)
            first.extensionCount = 1
            entity.booksOnHold.add(first)
            entity.booksOnHold.add(
                    new HoldDatabaseEntity(
                            entity,
                            anotherBookId.bookId,
                            patronId.patronId,
                            anotherBranchId.libraryBranchId,
                            anyDate))
        when:
            Set<PatronHoldSnapshot> patronHolds = domainModelMapper.mapPatronHolds(entity)
        then:
            patronHolds.size() == 2
            patronHolds.find { it.bookId() == bookId }.till() == anyDate
            patronHolds.find { it.bookId() == bookId }.extensionCount() == 1
    }

    def 'should map patron overdue checkouts'() {
        given:
            PatronDatabaseEntity entity = patronEntity(patronId, Regular)
            entity.checkouts.add(
                    new OverdueCheckoutDatabaseEntity(
                            entity,
                            bookId.bookId,
                            patronId.patronId,
                            libraryBranchId.libraryBranchId))
            entity.checkouts.add(
                    new OverdueCheckoutDatabaseEntity(
                            entity,
                            anotherBookId.bookId,
                            patronId.patronId,
                            anotherBranchId.libraryBranchId))
        when:
            Map<LibraryBranchId, Set<BookId>> overdueCheckouts =
                    domainModelMapper.mapPatronOverdueCheckouts(entity)
        then:
            overdueCheckouts.get(libraryBranchId).size() == 1
            overdueCheckouts.get(anotherBranchId).size() == 1
    }

    def 'should reconstruct suspended patron who rejects holds after mapping'() {
        given:
            PatronDatabaseEntity entity = patronEntity(patronId, Regular)
            entity.status = PatronStatus.SUSPENDED.name()
            entity.suspensionReason = "Policy violation"
        when:
            Patron patron = domainModelMapper.map(entity)
            def result = patron.placeOnHold(
                    circulatingAvailableBookAt(libraryBranchId),
                    closeEnded(anyDate, 3),
                    anyDate)
        then:
            result.rejection().isPresent()
    }

    PatronDatabaseEntity patronEntity(PatronId patronId, PatronType type) {
        return new PatronDatabaseEntity(
                patronId, type, EmailAddress.of("mapped@example.test"))
    }
}
