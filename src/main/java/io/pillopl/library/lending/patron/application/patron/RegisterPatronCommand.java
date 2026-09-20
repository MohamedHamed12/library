package io.pillopl.library.lending.patron.application.patron;

import java.time.Instant;
import java.util.Objects;

import io.pillopl.library.lending.patron.model.EmailAddress;
import io.pillopl.library.lending.patron.model.PatronType;

public record RegisterPatronCommand(Instant timestamp, PatronType type, EmailAddress emailAddress) {

  public RegisterPatronCommand {
    Objects.requireNonNull(timestamp, "timestamp");
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(emailAddress, "emailAddress");
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public PatronType getType() {
    return type;
  }

  public EmailAddress getEmailAddress() {
    return emailAddress;
  }
}
