package io.pillopl.library.lending.patronprofile.model;

import java.util.Objects;

import io.vavr.collection.List;

public record HoldsView(List<Hold> currentHolds) {

  public HoldsView {
    Objects.requireNonNull(currentHolds, "currentHolds");
  }

  public List<Hold> getCurrentHolds() {
    return currentHolds;
  }
}
