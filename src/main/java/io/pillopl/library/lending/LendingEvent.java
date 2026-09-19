package io.pillopl.library.lending;

import java.time.Instant;
import java.util.UUID;

import io.pillopl.library.commons.events.DomainEvent;

public interface LendingEvent extends DomainEvent {

  UUID getPatronId();

  UUID getBookId();

  UUID getLibraryBranchId();

  interface BookPlacedOnHoldEvent extends LendingEvent {
    Instant getHoldTill();
  }

  interface BookHoldExtendedEvent extends LendingEvent {
    Instant getHoldTill();
  }

  interface BookCheckedOutEvent extends LendingEvent {}

  interface BookHoldExpiredEvent extends LendingEvent {}

  interface BookHoldCanceledEvent extends LendingEvent {}

  interface BookReturnedEvent extends LendingEvent {}
}
