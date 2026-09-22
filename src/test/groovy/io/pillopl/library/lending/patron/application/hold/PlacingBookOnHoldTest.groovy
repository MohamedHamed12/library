package io.pillopl.library.lending.patron.application.hold

import io.pillopl.library.commons.commands.Result
import io.pillopl.library.lending.book.FindAvailableBook
import io.pillopl.library.lending.patron.model.Patron
import io.pillopl.library.lending.patron.model.PatronEvent
import io.pillopl.library.lending.patron.model.PatronFixture
import io.pillopl.library.lending.patron.model.Patrons
import io.pillopl.library.lending.patron.model.PatronId
import spock.lang.Specification

import java.time.Instant
import java.util.Optional

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.book.model.BookFixture.circulatingBook
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId
import static io.pillopl.library.lending.patron.model.PatronFixture.regularPatron

class PlacingBookOnHoldTest extends Specification {

    private static final Instant COMMAND_TIME = Instant.parse('2026-07-21T10:15:30Z')

    FindAvailableBook willFindBook = { id -> Optional.of(circulatingBook()) }
    FindAvailableBook willNotFindBook = { id -> Optional.empty() }
    Patrons repository = Stub()

    def 'should successfully place on hold book if patron and book exist'() {
        given:
            PlacingOnHold holding = new PlacingOnHold(willFindBook, repository)
        and:
            PatronId patron = persistedRegularPatron()
        expect:
            holding.placeOnHold(for3days(patron)) == Result.Success
    }

    def 'should reject placing on hold book if one of the domain rules is broken'() {
        given:
            PlacingOnHold holding = new PlacingOnHold(willFindBook, repository)
        and:
            PatronId patron = persistedRegularPatronWithManyHolds()
        expect:
            holding.placeOnHold(for3days(patron)) == Result.Rejection
    }

    def 'should fail if patron does not exist'() {
        given:
            PlacingOnHold holding = new PlacingOnHold(willFindBook, repository)
        and:
            PatronId patron = unknownPatron()
        when:
            holding.placeOnHold(for3days(patron))
        then:
            thrown(PatronNotFoundException)
    }

    def 'should fail if book does not exist'() {
        given:
            PlacingOnHold holding = new PlacingOnHold(willNotFindBook, repository)
        and:
            PatronId patron = persistedRegularPatron()
        when:
            holding.placeOnHold(for3days(patron))
        then:
            thrown(BookNotFoundException)
    }

    def 'should fail if saving patron fails'() {
        given:
            PlacingOnHold holding = new PlacingOnHold(willFindBook, repository)
        and:
            PatronId patron = persistedRegularPatronThatFailsOnSaving()
        when:
            holding.placeOnHold(for3days(patron))
        then:
            thrown(IllegalStateException)
    }

    PlaceOnHoldCommand for3days(PatronId patron) {
        return PlaceOnHoldCommand.closeEnded(
                COMMAND_TIME, patron, anyBranch(), anyBookId(), 4)
    }

    PatronId persistedRegularPatron() {
        PatronId patronId = anyPatronId()
        Patron patron = regularPatron(patronId)
        repository.findBy(patronId) >> Optional.of(patron)
        repository.publish(_ as PatronEvent) >> patron
        return patronId
    }

    PatronId persistedRegularPatronWithManyHolds() {
        PatronId patronId = anyPatronId()
        Patron patron = PatronFixture.regularPatronWithHolds(10)
        repository.findBy(patronId) >> Optional.of(patron)
        repository.publish(_ as PatronEvent) >> patron
        return patronId
    }

    PatronId persistedRegularPatronThatFailsOnSaving() {
        PatronId patronId = anyPatronId()
        Patron patron = regularPatron(patronId)
        repository.findBy(patronId) >> Optional.of(patron)
        repository.publish(_ as PatronEvent) >> { throw new IllegalStateException() }
        return patronId
    }

    PatronId unknownPatron() {
        PatronId patronId = anyPatronId()
        repository.findBy(patronId) >> Optional.empty()
        return patronId
    }
}
