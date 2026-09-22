package io.pillopl.library.lending.patron.application.checkout

import io.pillopl.library.commons.commands.Result
import io.pillopl.library.lending.book.FindBookOnHold
import io.pillopl.library.lending.book.model.BookOnHold
import io.pillopl.library.lending.patron.model.Patron
import io.pillopl.library.lending.patron.model.PatronEvent
import io.pillopl.library.lending.patron.model.Patrons
import io.pillopl.library.lending.patron.model.PatronId
import spock.lang.Specification

import java.time.Instant
import java.util.Optional

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.book.model.BookFixture.bookOnHold
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.*

class CheckingOutBookOnHoldTest extends Specification {

    private static final Instant CHECKOUT_TIME = Instant.parse('2026-07-21T10:15:30Z')

    BookOnHold bookOnHold = bookOnHold()
    PatronId patronId = anyPatronId()

    FindBookOnHold willFindBook = { bookId, patronId -> Optional.of(bookOnHold) }
    FindBookOnHold willNotFindBook = { bookId, patronId -> Optional.empty() }
    Patrons repository = Stub()

    def 'should successfully check out book if patron and book exist'() {
        given:
            CheckingOutBookOnHold checkingOut = new CheckingOutBookOnHold(willFindBook, repository)
        and:
            persisted(regularPatronWith(bookOnHold, patronId))
        expect:
            checkingOut.checkOut(for3days(patronId)) == Result.Success
    }

    def 'should reject checking out if one of the domain rules is broken'() {
        given:
            CheckingOutBookOnHold checkingOut = new CheckingOutBookOnHold(willFindBook, repository)
        and:
            persisted(regularPatron(patronId))
        expect:
            checkingOut.checkOut(for3days(patronId)) == Result.Rejection
    }

    def 'should fail if patron does not exist'() {
        given:
            CheckingOutBookOnHold checkingOut = new CheckingOutBookOnHold(willFindBook, repository)
        and:
            unknownPatron()
        when:
            checkingOut.checkOut(for3days(patronId))
        then:
            thrown(IllegalArgumentException)
    }

    def 'should fail if book does not exist'() {
        given:
            CheckingOutBookOnHold checkingOut = new CheckingOutBookOnHold(willNotFindBook, repository)
        and:
            persisted(regularPatronWith(bookOnHold, patronId))
        when:
            checkingOut.checkOut(for3days(patronId))
        then:
            thrown(IllegalArgumentException)
    }

    def 'should fail if saving patron fails'() {
        given:
            CheckingOutBookOnHold checkingOut = new CheckingOutBookOnHold(willFindBook, repository)
        and:
            persistedRegularPatronThatFailsOnSaving(patronId)
        when:
            checkingOut.checkOut(for3days(patronId))
        then:
            thrown(IllegalStateException)
    }

    CheckOutBookCommand for3days(PatronId patron) {
        return CheckOutBookCommand.create(CHECKOUT_TIME, patron, anyBranch(), anyBookId(), 4)
    }

    PatronId persisted(Patron patron) {
        repository.findBy(patronId) >> Optional.of(patron)
        repository.publish(_ as PatronEvent) >> patron
        return patronId
    }

    PatronId persistedRegularPatronThatFailsOnSaving(PatronId patronId) {
        Patron patron = regularPatron(patronId)
        repository.findBy(patronId) >> Optional.of(patron)
        repository.publish(_ as PatronEvent) >> { throw new IllegalStateException() }
        return patronId
    }

    PatronId unknownPatron() {
        repository.findBy(patronId) >> Optional.empty()
        return anyPatronId()
    }
}
