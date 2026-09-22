package io.pillopl.library.lending.patron.model

import io.pillopl.library.lending.book.model.AvailableBook
import io.pillopl.library.lending.book.model.BookOnHold
import io.pillopl.library.commons.commands.Decision
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.book.model.BookFixture.bookOnHold
import static io.pillopl.library.lending.book.model.BookFixture.circulatingBook
import static io.pillopl.library.lending.patron.model.CheckoutDuration.maxDuration
import static io.pillopl.library.lending.patron.model.HoldDuration.closeEnded
import static io.pillopl.library.lending.patron.model.PatronEvent.BookCheckingOutFailed
import static io.pillopl.library.lending.patron.model.PatronEvent.BookHoldFailed
import static io.pillopl.library.lending.patron.model.PatronEvent.PatronReactivated
import static io.pillopl.library.lending.patron.model.PatronEvent.PatronSuspended
import static io.pillopl.library.lending.patron.model.PatronFixture.regularPatron
import static io.pillopl.library.lending.patron.model.PatronFixture.suspendedRegularPatron
import static io.pillopl.library.lending.patron.model.PatronFixture.suspendedRegularPatronWithHold

class PatronTest extends Specification {

    private static final Instant TIMESTAMP = Instant.parse("2026-01-01T00:00:00Z")

    def 'active patron can be suspended with a reason'() {
        given:
            Patron patron = regularPatron()

        when:
            Decision<Rejection, PatronSuspended> result = patron.suspend("Repeated late returns", TIMESTAMP)

        then:
            result.success().isPresent()
            result.success().orElseThrow() instanceof PatronSuspended
            result.success().orElseThrow().reason == "Repeated late returns"
    }

    def 'already suspended patron cannot be suspended again'() {
        given:
            Patron patron = suspendedRegularPatron()

        when:
            Decision<Rejection, PatronSuspended> result = patron.suspend("Another reason", TIMESTAMP)

        then:
            result.rejection().isPresent()
    }

    def 'patron cannot be suspended with blank reason'() {
        given:
            Patron patron = regularPatron()

        expect:
            patron.suspend("", TIMESTAMP).rejection().isPresent()
            patron.suspend("   ", TIMESTAMP).rejection().isPresent()
    }

    def 'suspended patron cannot place a hold'() {
        given:
            Patron patron = suspendedRegularPatron()
            AvailableBook book = circulatingBook()

        when:
            Decision<BookHoldFailed, PatronEvent.BookPlacedOnHoldEvents> result =
                    patron.placeOnHold(book, closeEnded(TIMESTAMP, 3), TIMESTAMP)

        then:
            result.rejection().isPresent()
            result.rejection().orElseThrow() instanceof BookHoldFailed
            result.rejection().orElseThrow().reason.contains("suspended")
    }

    def 'suspended patron cannot check out a held book'() {
        given:
            BookOnHold book = bookOnHold()
            Patron patron = suspendedRegularPatronWithHold(book)

        when:
            Decision<BookCheckingOutFailed, PatronEvent.BookCheckedOut> result =
                    patron.checkOut(book, maxDuration(TIMESTAMP), TIMESTAMP)

        then:
            result.rejection().isPresent()
            result.rejection().orElseThrow() instanceof BookCheckingOutFailed
            result.rejection().orElseThrow().reason.contains("suspended")
    }

    def 'suspended patron can be reactivated'() {
        given:
            Patron patron = suspendedRegularPatron()

        when:
            Decision<Rejection, PatronReactivated> result = patron.reactivate(TIMESTAMP)

        then:
            result.success().isPresent()
            result.success().orElseThrow() instanceof PatronReactivated
    }

    def 'active patron cannot be reactivated'() {
        given:
            Patron patron = regularPatron()

        when:
            Decision<Rejection, PatronReactivated> result = patron.reactivate(TIMESTAMP)

        then:
            result.rejection().isPresent()
    }
}
