package io.pillopl.library.lending.patron.model


import io.vavr.control.Either
import spock.lang.Specification

import static PatronEvent.BookHoldFailed
import static PatronEvent.BookPlacedOnHoldEvents
import static PatronFixture.regularPatron
import static io.pillopl.library.lending.book.model.BookFixture.restrictedBook
import java.time.Instant
import static io.pillopl.library.lending.patron.model.HoldDuration.openEnded
class RegularPatronRequestingRestrictedBooksTest
        extends Specification {

    private static final Instant HOLD_TIME =
            Instant.parse('2026-07-21T10:15:30Z')

    def 'a regular patron cannot place restricted book on hold'() {
        when:
            Either<BookHoldFailed, BookPlacedOnHoldEvents> hold =
                    regularPatron().placeOnHold(
                            restrictedBook(),
                            openEnded(HOLD_TIME),
                            HOLD_TIME
                    )

        then:
            hold.isLeft()
    }
}