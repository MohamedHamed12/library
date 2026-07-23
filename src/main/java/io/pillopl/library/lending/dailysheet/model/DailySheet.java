package io.pillopl.library.lending.dailysheet.model;

import io.pillopl.library.lending.patron.model.PatronEvent.*;

import java.time.Instant;

public interface DailySheet {

    CheckoutsToOverdueSheet queryForCheckoutsToOverdue(Instant processingTime);

    HoldsToExpireSheet queryForHoldsToExpireSheet(Instant processingTime);

    void handle(BookPlacedOnHold event);

    void handle(BookHoldCanceled event);

    void handle(BookHoldExpired event);

    void handle(BookCheckedOut event);

    void handle(BookReturned event);


}
