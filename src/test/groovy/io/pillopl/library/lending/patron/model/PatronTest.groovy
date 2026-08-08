package io.pillopl.library.lending.patron.model

import io.pillopl.library.lending.book.model.AvailableBook
import io.pillopl.library.lending.book.model.BookOnHold
import io.vavr.control.Either
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
            Either<Rejection, PatronSuspended> result = patron.suspend("Repeated late returns", TIMESTAMP)

        then:
            result.isRight()
            result.get() instanceof PatronSuspended
            result.get().reason == "Repeated late returns"
    }

    def 'already suspended patron cannot be suspended again'() {
        given:
            Patron patron = suspendedRegularPatron()

        when:
            Either<Rejection, PatronSuspended> result = patron.suspend("Another reason", TIMESTAMP)

        then:
            result.isLeft()
    }

    def 'patron cannot be suspended with blank reason'() {
        given:
            Patron patron = regularPatron()

        expect:
            patron.suspend("", TIMESTAMP).isLeft()
            patron.suspend("   ", TIMESTAMP).isLeft()
    }

    def 'suspended patron cannot place a hold'() {
        given:
            Patron patron = suspendedRegularPatron()
            AvailableBook book = circulatingBook()

        when:
            Either<BookHoldFailed, PatronEvent.BookPlacedOnHoldEvents> result =
                    patron.placeOnHold(book, closeEnded(TIMESTAMP, 3), TIMESTAMP)

        then:
            result.isLeft()
            result.getLeft() instanceof BookHoldFailed
            result.getLeft().reason.contains("suspended")
    }

    def 'suspended patron cannot check out a held book'() {
        given:
            BookOnHold book = bookOnHold()
            Patron patron = suspendedRegularPatronWithHold(book)

        when:
            Either<BookCheckingOutFailed, PatronEvent.BookCheckedOut> result =
                    patron.checkOut(book, maxDuration(TIMESTAMP), TIMESTAMP)

        then:
            result.isLeft()
            result.getLeft() instanceof BookCheckingOutFailed
            result.getLeft().reason.contains("suspended")
    }

    def 'suspended patron can be reactivated'() {
        given:
            Patron patron = suspendedRegularPatron()

        when:
            Either<Rejection, PatronReactivated> result = patron.reactivate(TIMESTAMP)

        then:
            result.isRight()
            result.get() instanceof PatronReactivated
    }

    def 'active patron cannot be reactivated'() {
        given:
            Patron patron = regularPatron()

        when:
            Either<Rejection, PatronReactivated> result = patron.reactivate(TIMESTAMP)

        then:
            result.isLeft()
    }
}
