package io.pillopl.library.lending.patronprofile.model;

import java.util.List;
import java.util.Objects;

public record CheckoutsView(List<Checkout> currentCheckouts) {

  public CheckoutsView {
    Objects.requireNonNull(currentCheckouts, "currentCheckouts");
  }

  public List<Checkout> getCurrentCheckouts() {
    return currentCheckouts;
  }
}
