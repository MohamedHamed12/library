package io.pillopl.library.lending.patron.model

import io.pillopl.library.lending.book.model.AvailableBook
import io.vavr.control.Either
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.book.model.BookFixture.circulatingBook
import static io.pillopl.library.lending.patron.model.HoldDuration.closeEnded
import static io.pillopl.library.lending.patron.model.PatronEvent.*
import static io.pillopl.library.lending.patron.model.PatronFixture.regularPatronWithHolds

class PatronRequestingLastPossibleHoldTest
        extends Specification {

    private static final Instant HOLD_TIME =
            Instant.parse('2026-07-21T10:15:30Z')

    def 'should announce that a regular patron places his last possible hold'() {
        given:
            AvailableBook book = circulatingBook()

        when:
            Either<BookHoldFailed, BookPlacedOnHoldEvents> hold =
                    regularPatronWithHolds(4)
                            .placeOnHold(
                                    book,
                                    closeEnded(HOLD_TIME, 3),
                                    HOLD_TIME
                            )

        then:
            hold.isRight()

            hold.get().with {
                assert maximumNumberOhHoldsReached.isDefined()

                MaximumNumberOhHoldsReached event =
                        maximumNumberOhHoldsReached.get()

                assert event.numberOfHolds == 5
            }
    }
}