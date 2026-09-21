package io.pillopl.library.commons.commands;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public sealed interface Decision<F, S> permits Decision.Rejected, Decision.Accepted {

  static <F, S> Decision<F, S> rejected(F failure) {
    return new Rejected<>(failure);
  }

  static <F, S> Decision<F, S> accepted(S success) {
    return new Accepted<>(success);
  }

  <T> T fold(
      Function<? super F, ? extends T> onRejected, Function<? super S, ? extends T> onAccepted);

  Optional<F> rejection();

  Optional<S> success();

  record Rejected<F, S>(F failure) implements Decision<F, S> {

    public Rejected {
      Objects.requireNonNull(failure, "failure");
    }

    @Override
    public <T> T fold(
        Function<? super F, ? extends T> onRejected, Function<? super S, ? extends T> onAccepted) {
      return onRejected.apply(failure);
    }

    @Override
    public Optional<F> rejection() {
      return Optional.of(failure);
    }

    @Override
    public Optional<S> success() {
      return Optional.empty();
    }
  }

  record Accepted<F, S>(S value) implements Decision<F, S> {

    public Accepted {
      Objects.requireNonNull(value, "value");
    }

    @Override
    public <T> T fold(
        Function<? super F, ? extends T> onRejected, Function<? super S, ? extends T> onAccepted) {
      return onAccepted.apply(value);
    }

    @Override
    public Optional<F> rejection() {
      return Optional.empty();
    }

    @Override
    public Optional<S> success() {
      return Optional.of(value);
    }
  }
}
