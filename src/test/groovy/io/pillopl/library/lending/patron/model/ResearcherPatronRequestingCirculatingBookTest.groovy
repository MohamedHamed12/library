package io.pillopl.library.lending.patron.model


import io.vavr.control.Either
import spock.lang.Specification

import static PatronFixture.researcherPatronWithHolds
import static io.pillopl.library.lending.book.model.BookFixture.circulatingBook
import static PatronEvent.BookHoldFailed
import static PatronEvent.BookPlacedOnHoldEvents
import java.time.Instant
import static io.pillopl.library.lending.patron.model.HoldDuration.openEnded
class ResearcherPatronRequestingCirculatingBookTest extends Specification {

    private static final Instant HOLD_TIME =
        Instant.parse('2026-07-21T10:15:30Z')
    def 'a researcher patron can hold any number of circulating books'() {
        when:
            Either<BookHoldFailed, BookPlacedOnHoldEvents> hold = researcherPatronWithHolds(holds)
        .placeOnHold(
                circulatingBook(),
                openEnded(HOLD_TIME),
                HOLD_TIME
        )
        then:
            hold.isRight()
        where:
            holds << [0, 1, 2, 3, 4, 5, 100000]

    }
}
