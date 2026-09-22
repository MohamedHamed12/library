package io.pillopl.library.lending.patronprofile.model;

import java.util.List;
import java.util.Objects;

public record HoldsView(List<Hold> currentHolds) {

  public HoldsView {
    Objects.requireNonNull(currentHolds, "currentHolds");
  }

  public List<Hold> getCurrentHolds() {
    return currentHolds;
  }
}
