package io.pillopl.library.lending.patron.application.patron;

import java.time.Instant;
import java.util.Objects;

import io.pillopl.library.lending.patron.model.PatronId;

public record SuspendPatronCommand(Instant timestamp, PatronId patronId, String reason) {

  public SuspendPatronCommand {
    Objects.requireNonNull(timestamp, "timestamp");
    Objects.requireNonNull(patronId, "patronId");
    Objects.requireNonNull(reason, "reason");
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public PatronId getPatronId() {
    return patronId;
  }

  public String getReason() {
    return reason;
  }
}
