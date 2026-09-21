package io.pillopl.library.lending.patron.model

import io.pillopl.library.lending.book.model.AvailableBook
import io.pillopl.library.commons.commands.Decision
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
            Decision<BookHoldFailed, BookPlacedOnHoldEvents> hold =
                    regularPatronWithHolds(4)
                            .placeOnHold(
                                    book,
                                    closeEnded(HOLD_TIME, 3),
                                    HOLD_TIME
                            )

        then:
            hold.success().isPresent()

            verifyAll(hold.success().orElseThrow()) {
                assert maximumNumberOhHoldsReached.isPresent()

                MaximumNumberOhHoldsReached event =
                        maximumNumberOhHoldsReached.orElseThrow()

                assert event.numberOfHolds == 5
            }
    }
}
