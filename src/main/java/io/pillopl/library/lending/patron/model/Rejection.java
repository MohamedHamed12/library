package io.pillopl.library.lending.patron.model;

import java.util.Objects;

public record Rejection(Reason reason) {

  public Rejection {
    Objects.requireNonNull(reason, "reason");
  }

  public Reason getReason() {
    return reason;
  }

  public record Reason(String reason) {

    public Reason {
      Objects.requireNonNull(reason, "reason");
    }

    public String getReason() {
      return reason;
    }
  }

  static Rejection withReason(String reason) {
    return new Rejection(new Reason(reason));
  }
}
