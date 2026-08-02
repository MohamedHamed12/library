package io.pillopl.library.lending.patron.application.patron;

import io.pillopl.library.lending.patron.model.PatronType;
import java.time.Instant;
import lombok.NonNull;
import lombok.Value;

@Value
public class RegisterPatronCommand {
    @NonNull Instant timestamp;
    @NonNull PatronType type;
}