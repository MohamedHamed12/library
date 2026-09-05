package io.pillopl.library.lending.patron.application.patron;

import io.pillopl.library.lending.patron.model.PatronId;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;

@Value
public class SuspendPatronCommand {
    @NonNull Instant timestamp;
    @NonNull PatronId patronId;
    @NonNull String reason;
}
