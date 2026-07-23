package io.pillopl.library.lending.patron.application.hold;

import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.book.model.BookDuplicateHoldFound;
import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Try;
import org.springframework.context.event.EventListener;

import java.time.Clock;
import java.time.Instant;

public class HandleDuplicateHold {

    private final CancelingHold cancelingHold;
    private final Clock clock;

    public HandleDuplicateHold(CancelingHold cancelingHold, Clock clock) {
        this.cancelingHold = cancelingHold;
        this.clock = clock;
    }

    @EventListener
    public Try<Result> handle(BookDuplicateHoldFound event) {
        return cancelingHold.cancelHold(cancelHoldCommandFrom(event));
    }

    private CancelHoldCommand cancelHoldCommandFrom(BookDuplicateHoldFound event) {
        Instant cancellationTime = clock.instant();
        return new CancelHoldCommand(
                cancellationTime,
                new PatronId(event.getSecondPatronId()),
                new BookId(event.getBookId()));
    }

}

