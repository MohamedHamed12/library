package io.pillopl.library.lending.patron.application.checkout;

import io.pillopl.library.commons.commands.BatchResult;
import io.pillopl.library.lending.dailysheet.model.DailySheet;
import io.pillopl.library.lending.patron.model.PatronEvent.OverdueCheckoutRegistered;
import io.pillopl.library.lending.patron.model.Patrons;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;

import java.time.Clock;
import java.time.Instant;

@AllArgsConstructor
public class RegisteringOverdueCheckout {

    private final DailySheet find;
    private final Patrons patronRepository;
    private final Clock clock;

    public Try<BatchResult> registerOverdueCheckouts() {
        return Try.of(() -> {
            Instant processingTime = clock.instant();
            return find.queryForCheckoutsToOverdue(processingTime)
                .toStreamOfEvents(processingTime)
                .map(this::publish)
                .find(Try::isFailure)
                .map(handleEventError -> BatchResult.SomeFailed)
                .getOrElse(BatchResult.FullSuccess);
        });
    }

    private Try<Void> publish(OverdueCheckoutRegistered event) {
        return Try.run(() -> patronRepository.publish(event));
    }

}
