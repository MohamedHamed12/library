package io.pillopl.library.lending.patronprofile.model;

import java.util.Objects;

import io.vavr.collection.List;

public record CheckoutsView(List<Checkout> currentCheckouts) {

  public CheckoutsView {
    Objects.requireNonNull(currentCheckouts, "currentCheckouts");
  }

  public List<Checkout> getCurrentCheckouts() {
    return currentCheckouts;
  }
}
