package io.pillopl.library.lending.patron.model;

import static io.pillopl.library.lending.patron.model.PatronType.Regular;

import java.util.Objects;

record PatronInformation(PatronId patronId, PatronType type, EmailAddress emailAddress) {

  PatronInformation {
    Objects.requireNonNull(patronId, "patronId");
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(emailAddress, "emailAddress");
  }

  PatronId getPatronId() {
    return patronId;
  }

  PatronType getType() {
    return type;
  }

  EmailAddress getEmailAddress() {
    return emailAddress;
  }

  boolean isRegular() {
    return type.equals(Regular);
  }
}
