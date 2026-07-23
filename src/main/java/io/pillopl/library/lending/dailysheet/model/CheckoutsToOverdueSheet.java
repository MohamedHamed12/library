package io.pillopl.library.lending.dailysheet.model;

import io.pillopl.library.lending.patron.model.PatronEvent.OverdueCheckoutRegistered;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;

@Value
public class CheckoutsToOverdueSheet {

    @NonNull
    List<OverdueCheckout> checkouts;

    public Stream<OverdueCheckoutRegistered> toStreamOfEvents(Instant processingTime) {
        return checkouts.toStream()
                .map(checkout -> checkout.toEvent(processingTime));
    }

    public int count() {
        return checkouts.size();
    }

}
