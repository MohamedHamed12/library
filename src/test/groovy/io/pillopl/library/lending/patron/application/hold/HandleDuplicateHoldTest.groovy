package io.pillopl.library.lending.patron.application.hold

import io.pillopl.library.catalogue.BookId
import io.pillopl.library.lending.book.model.BookDuplicateHoldFound
import io.pillopl.library.lending.patron.model.PatronId
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId

class HandleDuplicateHoldTest extends Specification {

    private static final Instant DETECTION_TIME =
            Instant.parse('2026-07-21T10:00:00Z')

    private static final Instant CANCELLATION_TIME =
            Instant.parse('2026-07-21T10:00:01Z')

    CancelingHold cancelingHold = Mock()

    def "should start cancelling hold if book was already hold by other patron"() {
        given:
            Clock clock = Clock.fixed(CANCELLATION_TIME, ZoneOffset.UTC)
        and:
            HandleDuplicateHold duplicateHold = new HandleDuplicateHold(cancelingHold, clock)
        and:
            BookDuplicateHoldFound bookDuplicateHoldFound = duplicateHoldFoundBy()
        and:
            CancelHoldCommand cancelHoldCommand = cancelHoldCommandFrom(bookDuplicateHoldFound, clock)
        when:
            duplicateHold.handle(bookDuplicateHoldFound)
        then:
            1 * cancelingHold.cancelHold(cancelHoldCommand)
    }

    BookDuplicateHoldFound duplicateHoldFoundBy() {
        return new BookDuplicateHoldFound(
                DETECTION_TIME,
                anyPatronId().patronId,
                anyPatronId().patronId,
                anyBranch().libraryBranchId,
                anyBookId().bookId
        )
    }

    CancelHoldCommand cancelHoldCommandFrom(BookDuplicateHoldFound event, Clock clock) {
        return new CancelHoldCommand(
                clock.instant(),
                new PatronId(event.getSecondPatronId()),
                new BookId(event.getBookId())
        )
    }

}
