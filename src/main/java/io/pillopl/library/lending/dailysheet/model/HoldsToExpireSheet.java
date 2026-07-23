package io.pillopl.library.lending.dailysheet.model;

import io.pillopl.library.lending.patron.model.PatronEvent;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;

@Value
public class HoldsToExpireSheet {

    @NonNull
    List<ExpiredHold> expiredHolds;

    public Stream<PatronEvent.BookHoldExpired> toStreamOfEvents(Instant processingTime) {
        return expiredHolds
                .toStream()
                .map(expiredHold -> expiredHold.toEvent(processingTime));
    }

    public int count() {
        return expiredHolds.size();
    }

}
