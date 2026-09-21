package io.pillopl.library.lending.patron.application.hold

import io.pillopl.library.commons.commands.Result
import io.pillopl.library.lending.book.FindBookOnHold
import io.pillopl.library.lending.book.model.BookOnHold
import io.pillopl.library.lending.patron.model.*
import spock.lang.Specification

import java.time.Instant
import java.util.Optional

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.book.model.BookFixture.bookOnHold
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId

class CancelingHoldTest extends Specification {

    private static final Instant COMMAND_TIME = Instant.parse('2026-07-21T11:30:00Z')

    BookOnHold bookOnHold = bookOnHold()
    PatronId patronId = anyPatronId()

    FindBookOnHold willFindBook = { bookId, patronId -> Optional.of(bookOnHold) }
    FindBookOnHold willNotFindBook = { bookId, patronId -> Optional.empty() }
    Patrons repository = Stub()

    def 'should successfully cancel hold if book was placed on hold by patron and patron and book exist'() {
        given:
            CancelingHold canceling = new CancelingHold(willFindBook, repository)
        and:
            persistedRegularPatronWithBookOnHold()
        expect:
            canceling.cancelHold(cmd()) == Result.Success
    }

    def 'should reject canceling hold if one of the domain rules is broken'() {
        given:
            CancelingHold canceling = new CancelingHold(willFindBook, repository)
        and:
            persistedRegularPatronWithoutBookOnHold()
        expect:
            canceling.cancelHold(cmd()) == Result.Rejection
    }

    def 'should fail if patron does not exist'() {
        given:
            CancelingHold canceling = new CancelingHold(willFindBook, repository)
        and:
            unknownPatron()
        when:
            canceling.cancelHold(cmd())
        then:
            thrown(PatronNotFoundException)
    }

    def 'should fail if book does not exist'() {
        given:
            CancelingHold canceling = new CancelingHold(willNotFindBook, repository)
        and:
            persistedRegularPatronWithBookOnHold()
        when:
            canceling.cancelHold(cmd())
        then:
            thrown(HoldNotFoundException)
    }

    def 'should fail if saving patron fails'() {
        given:
            CancelingHold canceling = new CancelingHold(willFindBook, repository)
        and:
            persistedRegularPatronThatFailsOnSaving()
        when:
            canceling.cancelHold(cmd())
        then:
            thrown(IllegalStateException)
    }

    CancelHoldCommand cmd() {
        return new CancelHoldCommand(COMMAND_TIME, patronId, anyBookId())
    }

    PatronId persistedRegularPatronWithBookOnHold() {
        Patron patron = PatronFixture.regularPatronWithHold(bookOnHold)
        repository.findBy(patronId) >> Optional.of(patron)
        repository.publish(_ as PatronEvent) >> patron
        return patronId
    }

    PatronId persistedRegularPatronWithoutBookOnHold() {
        Patron patron = PatronFixture.regularPatronWithHolds(10)
        repository.findBy(patronId) >> Optional.of(patron)
        return patronId
    }

    PatronId persistedRegularPatronThatFailsOnSaving() {
        Patron patron = PatronFixture.regularPatronWithHold(bookOnHold)
        repository.findBy(patronId) >> Optional.of(patron)
        repository.publish(_ as PatronEvent) >> { throw new IllegalStateException() }
        return patronId
    }

    PatronId unknownPatron() {
        repository.findBy(patronId) >> Optional.empty()
        return patronId
    }
}
