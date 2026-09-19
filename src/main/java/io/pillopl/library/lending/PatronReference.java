package io.pillopl.library.lending;

import java.util.Objects;
import java.util.UUID;

public class PatronReference {

  private final UUID patronId;

  public PatronReference(UUID patronId) {
    this.patronId = Objects.requireNonNull(patronId, "patronId");
  }

  public UUID getPatronId() {
    return patronId;
  }

  public static PatronReference of(UUID patronId) {
    return new PatronReference(patronId);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof PatronReference that)) {
      return false;
    }
    return patronId.equals(that.patronId);
  }

  @Override
  public int hashCode() {
    return patronId.hashCode();
  }
}
