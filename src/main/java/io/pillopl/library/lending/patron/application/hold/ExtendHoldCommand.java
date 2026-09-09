package io.pillopl.library.lending.patron.application.hold;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.patron.model.NumberOfDays;
import io.pillopl.library.lending.patron.model.PatronId;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;

@Value
public class ExtendHoldCommand {
    @NonNull Instant timestamp;
    @NonNull PatronId patronId;
    @NonNull BookId bookId;
    @NonNull NumberOfDays additionalDays;
}
