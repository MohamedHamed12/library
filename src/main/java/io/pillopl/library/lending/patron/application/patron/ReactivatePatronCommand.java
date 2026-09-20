package io.pillopl.library.lending.patron.application.patron;

import java.time.Instant;
import java.util.Objects;

import io.pillopl.library.lending.patron.model.PatronId;

public record ReactivatePatronCommand(Instant timestamp, PatronId patronId) {

  public ReactivatePatronCommand {
    Objects.requireNonNull(timestamp, "timestamp");
    Objects.requireNonNull(patronId, "patronId");
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public PatronId getPatronId() {
    return patronId;
  }
}
