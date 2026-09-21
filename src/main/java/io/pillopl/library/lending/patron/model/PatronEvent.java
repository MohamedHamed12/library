package io.pillopl.library.lending.patron.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.events.DomainEvent;
import io.pillopl.library.lending.LendingEvent;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.vavr.collection.List;
import io.vavr.control.Option;

public sealed interface PatronEvent extends DomainEvent {

  default PatronId patronId() {
    return new PatronId(getPatronId());
  }

  UUID getPatronId();

  @Override
  default UUID getAggregateId() {
    return getPatronId();
  }

  default List<DomainEvent> normalize() {
    return List.of(this);
  }

  record PatronCreated(
      UUID eventId, Instant when, UUID patronId, PatronType patronType, EmailAddress emailAddress)
      implements PatronEvent {

    public PatronCreated {
      Objects.requireNonNull(eventId, "eventId");
      Objects.requireNonNull(when, "when");
      Objects.requireNonNull(patronId, "patronId");
      Objects.requireNonNull(patronType, "patronType");
      Objects.requireNonNull(emailAddress, "emailAddress");
    }

    public PatronCreated(
        Instant when, UUID patronId, PatronType patronType, EmailAddress emailAddress) {
      this(UUID.randomUUID(), when, patronId, patronType, emailAddress);
    }

    public static PatronCreated createdAt(
        Instant timestamp, PatronId patronId, PatronType type, EmailAddress emailAddress) {
      return new PatronCreated(timestamp, patronId.getPatronId(), type, emailAddress);
    }

    @Override
    public UUID getEventId() {
      return eventId;
    }

    @Override
    public Instant getWhen() {
      return when;
    }

    @Override
    public UUID getPatronId() {
      return patronId;
    }

    public PatronType getPatronType() {
      return patronType;
    }

    public EmailAddress getEmailAddress() {
      return emailAddress;
    }
  }

  record BookPlacedOnHold(
      UUID eventId,
      Instant when,
      UUID patronId,
      UUID bookId,
      BookType bookType,
      UUID libraryBranchId,
      Instant holdFrom,
      Instant holdTill)
      implements PatronEvent, LendingEvent.BookPlacedOnHoldEvent {

    public BookPlacedOnHold {
      Objects.requireNonNull(eventId, "eventId");
      Objects.requireNonNull(when, "when");
      Objects.requireNonNull(patronId, "patronId");
      Objects.requireNonNull(bookId, "bookId");
      Objects.requireNonNull(bookType, "bookType");
      Objects.requireNonNull(libraryBranchId, "libraryBranchId");
      Objects.requireNonNull(holdFrom, "holdFrom");
    }

    public BookPlacedOnHold(
        Instant when,
        UUID patronId,
        UUID bookId,
        BookType bookType,
        UUID libraryBranchId,
        Instant holdFrom,
        Instant holdTill) {
      this(
          UUID.randomUUID(),
          when,
          patronId,
          bookId,
          bookType,
          libraryBranchId,
          holdFrom,
          holdTill);
    }

    public static BookPlacedOnHold placedOnHoldAt(
        Instant timestamp,
        BookId bookId,
        BookType bookType,
        LibraryBranchId libraryBranchId,
        PatronId patronId,
        HoldDuration holdDuration) {
      return new BookPlacedOnHold(
          timestamp,
          patronId.getPatronId(),
          bookId.getBookId(),
          bookType,
          libraryBranchId.getLibraryBranchId(),
          holdDuration.getFrom(),
          holdDuration.getTo().getOrNull());
    }

    @Override
    public UUID getEventId() {
      return eventId;
    }

    @Override
    public Instant getWhen() {
      return when;
    }

    @Override
    public UUID getPatronId() {
      return patronId;
    }

    @Override
    public UUID getBookId() {
      return bookId;
    }

    public BookType getBookType() {
      return bookType;
    }

    @Override
    public UUID getLibraryBranchId() {
      return libraryBranchId;
    }

    public Instant getHoldFrom() {
      return holdFrom;
    }

    @Override
    public Instant getHoldTill() {
      return holdTill;
    }
  }

  record BookPlacedOnHoldEvents(
      UUID eventId,
      UUID patronId,
      BookPlacedOnHold bookPlacedOnHold,
      Option<MaximumNumberOhHoldsReached> maximumNumberOhHoldsReached)
      implements PatronEvent {

    public BookPlacedOnHoldEvents {
      Objects.requireNonNull(eventId, "eventId");
      Objects.requireNonNull(patronId, "patronId");
      Objects.requireNonNull(bookPlacedOnHold, "bookPlacedOnHold");
      Objects.requireNonNull(maximumNumberOhHoldsReached, "maximumNumberOhHoldsReached");
    }

    public BookPlacedOnHoldEvents(
        UUID patronId,
        BookPlacedOnHold bookPlacedOnHold,
        Option<MaximumNumberOhHoldsReached> maximumNumberOhHoldsReached) {
      this(UUID.randomUUID(), patronId, bookPlacedOnHold, maximumNumberOhHoldsReached);
    }

    @Override
    public UUID getEventId() {
      return eventId;
    }

    @Override
    public UUID getPatronId() {
      return patronId;
    }

    public BookPlacedOnHold getBookPlacedOnHold() {
      return bookPlacedOnHold;
    }

    public Option<MaximumNumberOhHoldsReached> getMaximumNumberOhHoldsReached() {
      return maximumNumberOhHoldsReached;
    }

    @Override
    public Instant getWhen() {
      return bookPlacedOnHold.when();
    }

    public static BookPlacedOnHoldEvents events(BookPlacedOnHold bookPlacedOnHold) {
      return new BookPlacedOnHoldEvents(
          bookPlacedOnHold.getPatronId(), bookPlacedOnHold, Option.none());
    }

    public static BookPlacedOnHoldEvents events(
        BookPlacedOnHold bookPlacedOnHold,
        MaximumNumberOhHoldsReached maximumNumberOhHoldsReached) {
      return new BookPlacedOnHoldEvents(
          bookPlacedOnHold.getPatronId(),
          bookPlacedOnHold,
          Option.of(maximumNumberOhHoldsReached));
    }

    @Override
    public List<DomainEvent> normalize() {
      return List.<DomainEvent>of(bookPlacedOnHold)
          .appendAll(maximumNumberOhHoldsReached.toList());
    }
  }

  record MaximumNumberOhHoldsReached(
      UUID eventId, Instant when, UUID patronId, int numberOfHolds) implements PatronEvent {

    public MaximumNumberOhHoldsReached {
      Objects.requireNonNull(eventId, "eventId");
      Objects.requireNonNull(when, "when");
      Objects.requireNonNull(patronId, "patronId");
    }

    public MaximumNumberOhHoldsReached(Instant when, UUID patronId, int numberOfHolds) {
      this(UUID.randomUUID(), when, patronId, numberOfHolds);
    }

    public static MaximumNumberOhHoldsReached reachedAt(
        Instant timestamp, PatronInformation patronInformation, int numberOfHolds) {
      return new MaximumNumberOhHoldsReached(
          timestamp, patronInformation.getPatronId().getPatronId(), numberOfHolds);
    }

    @Override
    public UUID getEventId() {
      return eventId;
    }

    @Override
    public Instant getWhen() {
      return when;
    }

    @Override
    public UUID getPatronId() {
      return patronId;
    }

    public int getNumberOfHolds() {
      return numberOfHolds;
    }
  }

  record BookCheckedOut(
      UUID eventId,
      Instant when,
      UUID patronId,
      UUID bookId,
      BookType bookType,
      UUID libraryBranchId,
      Instant till)
      implements PatronEvent, LendingEvent.BookCheckedOutEvent {

    public BookCheckedOut {
      Objects.requireNonNull(eventId, "eventId");
      Objects.requireNonNull(when, "when");
      Objects.requireNonNull(patronId, "patronId");
      Objects.requireNonNull(bookId, "bookId");
      Objects.requireNonNull(bookType, "bookType");
      Objects.requireNonNull(libraryBranchId, "libraryBranchId");
      Objects.requireNonNull(till, "till");
    }

    public BookCheckedOut(
        Instant when,
        UUID patronId,
        UUID bookId,
        BookType bookType,
        UUID libraryBranchId,
        Instant till) {
      this(UUID.randomUUID(), when, patronId, bookId, bookType, libraryBranchId, till);
    }

    public static BookCheckedOut checkedOutAt(
        Instant timestamp,
        BookId bookId,
        BookType bookType,
        LibraryBranchId libraryBranchId,
        PatronId patronId,
        CheckoutDuration checkoutDuration) {
      return new BookCheckedOut(
          timestamp,
          patronId.getPatronId(),
          bookId.getBookId(),
          bookType,
          libraryBranchId.getLibraryBranchId(),
          checkoutDuration.to());
    }

    @Override
    public UUID getEventId() {
      return eventId;
    }

    @Override
    public Instant getWhen() {
      return when;
    }

    @Override
    public UUID getPatronId() {
      return patronId;
    }

    @Override
    public UUID getBookId() {
      return bookId;
    }

    public BookType getBookType() {
      return bookType;
    }

    @Override
    public UUID getLibraryBranchId() {
      return libraryBranchId;
    }

    public Instant getTill() {
      return till;
    }
  }

  record BookReturned(
      UUID eventId,
      Instant when,
      UUID patronId,
      UUID bookId,
      BookType bookType,
      UUID libraryBranchId)
      implements PatronEvent, LendingEvent.BookReturnedEvent {

    public BookReturned {
      Objects.requireNonNull(eventId, "eventId");
      Objects.requireNonNull(when, "when");
      Objects.requireNonNull(patronId, "patronId");
      Objects.requireNonNull(bookId, "bookId");
      Objects.requireNonNull(bookType, "bookType");
      Objects.requireNonNull(libraryBranchId, "libraryBranchId");
    }

    public BookReturned(
        Instant when, UUID patronId, UUID bookId, BookType bookType, UUID libraryBranchId) {
      this(UUID.randomUUID(), when, patronId, bookId, bookType, libraryBranchId);
    }

    @Override
    public UUID getEventId() {
      return eventId;
    }

    @Override
    public Instant getWhen() {
      return when;
    }

    @Override
    public UUID getPatronId() {
      return patronId;
    }

    @Override
    public UUID getBookId() {
      return bookId;
    }

    public BookType getBookType() {
      return bookType;
    }

    @Override
    public UUID getLibraryBranchId() {
      return libraryBranchId;
    }
  }

  record BookHoldFailed(
      UUID eventId,
      String reason,
      Instant when,
      UUID patronId,
      UUID bookId,
      UUID libraryBranchId)
      implements PatronEvent {

    public BookHoldFailed {
      Objects.requireNonNull(eventId, "eventId");
      Objects.requireNonNull(reason, "reason");
      Objects.requireNonNull(when, "when");
      Objects.requireNonNull(patronId, "patronId");
      Objects.requireNonNull(bookId, "bookId");
      Objects.requireNonNull(libraryBranchId, "libraryBranchId");
    }

    public BookHoldFailed(
        String reason, Instant when, UUID patronId, UUID bookId, UUID libraryBranchId) {
      this(UUID.randomUUID(), reason, when, patronId, bookId, libraryBranchId);
    }

    static BookHoldFailed holdFailedAt(
        Instant timestamp,
        Rejection rejection,
        BookId bookId,
        LibraryBranchId libraryBranchId,
        PatronInformation patronInformation) {
      return new BookHoldFailed(
          rejection.getReason().getReason(),
          timestamp,
          patronInformation.getPatronId().getPatronId(),
          bookId.getBookId(),
          libraryBranchId.getLibraryBranchId());
    }

    @Override
    public UUID getEventId() {
      return eventId;
    }

    public String getReason() {
      return reason;
    }

    @Override
    public Instant getWhen() {
      return when;
    }

    @Override
    public UUID getPatronId() {
      return patronId;
    }

    public UUID getBookId() {
      return bookId;
    }

    public UUID getLibraryBranchId() {
      return libraryBranchId;
    }
  }

  record BookCheckingOutFailed(
      UUID eventId,
      String reason,
      Instant when,
      UUID patronId,
      UUID bookId,
      UUID libraryBranchId)
      implements PatronEvent {

    public BookCheckingOutFailed {
      Objects.requireNonNull(eventId, "eventId");
      Objects.requireNonNull(reason, "reason");
      Objects.requireNonNull(when, "when");
      Objects.requireNonNull(patronId, "patronId");
      Objects.requireNonNull(bookId, "bookId");
      Objects.requireNonNull(libraryBranchId, "libraryBranchId");
    }

    public BookCheckingOutFailed(
        String reason, Instant when, UUID patronId, UUID bookId, UUID libraryBranchId) {
      this(UUID.randomUUID(), reason, when, patronId, bookId, libraryBranchId);
    }

    static BookCheckingOutFailed checkoutFailedAt(
        Instant timestamp,
        Rejection rejection,
        BookId bookId,
        LibraryBranchId libraryBranchId,
        PatronInformation patronInformation) {
      return new BookCheckingOutFailed(
          rejection.getReason().getReason(),
          timestamp,
          patronInformation.getPatronId().getPatronId(),
          bookId.getBookId(),
          libraryBranchId.getLibraryBranchId());
    }

    @Override
    public UUID getEventId() {
      return eventId;
    }

    public String getReason() {
      return reason;
    }

    @Override
    public Instant getWhen() {
      return when;
    }

    @Override
    public UUID getPatronId() {
      return patronId;
    }

    public UUID getBookId() {
      return bookId;
    }

    public UUID getLibraryBranchId() {
      return libraryBranchId;
    }
  }

  record BookHoldExtended(
      UUID eventId,
      Instant when,
      UUID patronId,
      UUID bookId,
      UUID libraryBranchId,
      Instant previousHoldTill,
      Instant holdTill,
      int extensionCount)
      implements PatronEvent, LendingEvent.BookHoldExtendedEvent {

    public BookHoldExtended {
      Objects.requireNonNull(eventId, "eventId");
      Objects.requireNonNull(when, "when");
      Objects.requireNonNull(patronId, "patronId");
      Objects.requireNonNull(bookId, "bookId");
      Objects.requireNonNull(libraryBranchId, "libraryBranchId");
      Objects.requireNonNull(previousHoldTill, "previousHoldTill");
      Objects.requireNonNull(holdTill, "holdTill");
    }

    public BookHoldExtended(
        Instant when,
        UUID patronId,
        UUID bookId,
        UUID libraryBranchId,
        Instant previousHoldTill,
        Instant holdTill,
        int extensionCount) {
      this(
          UUID.randomUUID(),
          when,
          patronId,
          bookId,
          libraryBranchId,
          previousHoldTill,
          holdTill,
          extensionCount);
    }

    public static BookHoldExtended extendedAt(
        Instant timestamp,
        BookId bookId,
        LibraryBranchId libraryBranchId,
        PatronId patronId,
        Instant previousHoldTill,
        Instant holdTill,
        int extensionCount) {
      return new BookHoldExtended(
          timestamp,
          patronId.getPatronId(),
          bookId.getBookId(),
          libraryBranchId.getLibraryBranchId(),
          previousHoldTill,
          holdTill,
          extensionCount);
    }

    @Override
    public UUID getEventId() {
      return eventId;
    }

    @Override
    public Instant getWhen() {
      return when;
    }

    @Override
    public UUID getPatronId() {
      return patronId;
    }

    @Override
    public UUID getBookId() {
      return bookId;
    }

    @Override
    public UUID getLibraryBranchId() {
      return libraryBranchId;
    }

    public Instant getPreviousHoldTill() {
      return previousHoldTill;
    }

    @Override
    public Instant getHoldTill() {
      return holdTill;
    }

    public int getExtensionCount() {
      return extensionCount;
    }
  }

  record BookHoldExtensionFailed(
      UUID eventId,
      String reason,
      Instant when,
      UUID patronId,
      UUID bookId,
      UUID libraryBranchId)
      implements PatronEvent {

    public BookHoldExtensionFailed {
      Objects.requireNonNull(eventId, "eventId");
      Objects.requireNonNull(reason, "reason");
      Objects.requireNonNull(when, "when");
      Objects.requireNonNull(patronId, "patronId");
      Objects.requireNonNull(bookId, "bookId");
      Objects.requireNonNull(libraryBranchId, "libraryBranchId");
    }

    public BookHoldExtensionFailed(
        String reason, Instant when, UUID patronId, UUID bookId, UUID libraryBranchId) {
      this(UUID.randomUUID(), reason, when, patronId, bookId, libraryBranchId);
    }

    static BookHoldExtensionFailed extensionFailedAt(
        Instant timestamp,
        Rejection rejection,
        BookId bookId,
        LibraryBranchId libraryBranchId,
        PatronInformation patronInformation) {
      return new BookHoldExtensionFailed(
          rejection.getReason().getReason(),
          timestamp,
          patronInformation.getPatronId().getPatronId(),
          bookId.getBookId(),
          libraryBranchId.getLibraryBranchId());
    }

    @Override
    public UUID getEventId() {
      return eventId;
    }

    public String getReason() {
      return reason;
    }

    @Override
    public Instant getWhen() {
      return when;
    }

    @Override
    public UUID getPatronId() {
      return patronId;
    }

    public UUID getBookId() {
      return bookId;
    }

    public UUID getLibraryBranchId() {
      return libraryBranchId;
    }
  }

  record BookHoldCanceled(
      UUID eventId, Instant when, UUID patronId, UUID bookId, UUID libraryBranchId)
      implements PatronEvent, LendingEvent.BookHoldCanceledEvent {

    public BookHoldCanceled {
      Objects.requireNonNull(eventId, "eventId");
      Objects.requireNonNull(when, "when");
      Objects.requireNonNull(patronId, "patronId");
      Objects.requireNonNull(bookId, "bookId");
      Objects.requireNonNull(libraryBranchId, "libraryBranchId");
    }

    public BookHoldCanceled(
        Instant when, UUID patronId, UUID bookId, UUID libraryBranchId) {
      this(UUID.randomUUID(), when, patronId, bookId, libraryBranchId);
    }

    public static BookHoldCanceled canceledAt(
        Instant timestamp, BookId bookId, LibraryBranchId libraryBranchId, PatronId patronId) {
      return new BookHoldCanceled(
          timestamp,
          patronId.getPatronId(),
          bookId.getBookId(),
          libraryBranchId.getLibraryBranchId());
    }

    @Override
    public UUID getEventId() {
      return eventId;
    }

    @Override
    public Instant getWhen() {
      return when;
    }

    @Override
    public UUID getPatronId() {
      return patronId;
    }

    @Override
    public UUID getBookId() {
      return bookId;
    }

    @Override
    public UUID getLibraryBranchId() {
      return libraryBranchId;
    }
  }

  record BookHoldCancelingFailed(
      UUID eventId, Instant when, UUID patronId, UUID bookId, UUID libraryBranchId)
      implements PatronEvent {

    public BookHoldCancelingFailed {
      Objects.requireNonNull(eventId, "eventId");
      Objects.requireNonNull(when, "when");
      Objects.requireNonNull(patronId, "patronId");
      Objects.requireNonNull(bookId, "bookId");
      Objects.requireNonNull(libraryBranchId, "libraryBranchId");
    }

    public BookHoldCancelingFailed(
        Instant when, UUID patronId, UUID bookId, UUID libraryBranchId) {
      this(UUID.randomUUID(), when, patronId, bookId, libraryBranchId);
    }

    static BookHoldCancelingFailed cancellationFailedAt(
        Instant timestamp, BookId bookId, LibraryBranchId libraryBranchId, PatronId patronId) {
      return new BookHoldCancelingFailed(
          timestamp,
          patronId.getPatronId(),
          bookId.getBookId(),
          libraryBranchId.getLibraryBranchId());
    }

    @Override
    public UUID getEventId() {
      return eventId;
    }

    @Override
    public Instant getWhen() {
      return when;
    }

    @Override
    public UUID getPatronId() {
      return patronId;
    }

    public UUID getBookId() {
      return bookId;
    }

    public UUID getLibraryBranchId() {
      return libraryBranchId;
    }
  }

  record BookHoldExpired(
      UUID eventId, Instant when, UUID patronId, UUID bookId, UUID libraryBranchId)
      implements PatronEvent, LendingEvent.BookHoldExpiredEvent {

    public BookHoldExpired {
      Objects.requireNonNull(eventId, "eventId");
      Objects.requireNonNull(when, "when");
      Objects.requireNonNull(patronId, "patronId");
      Objects.requireNonNull(bookId, "bookId");
      Objects.requireNonNull(libraryBranchId, "libraryBranchId");
    }

    public BookHoldExpired(Instant when, UUID patronId, UUID bookId, UUID libraryBranchId) {
      this(UUID.randomUUID(), when, patronId, bookId, libraryBranchId);
    }

    public static BookHoldExpired expiredAt(
        Instant timestamp, BookId bookId, PatronId patronId, LibraryBranchId libraryBranchId) {
      return new BookHoldExpired(
          timestamp,
          patronId.getPatronId(),
          bookId.getBookId(),
          libraryBranchId.getLibraryBranchId());
    }

    @Override
    public UUID getEventId() {
      return eventId;
    }

    @Override
    public Instant getWhen() {
      return when;
    }

    @Override
    public UUID getPatronId() {
      return patronId;
    }

    @Override
    public UUID getBookId() {
      return bookId;
    }

    @Override
    public UUID getLibraryBranchId() {
      return libraryBranchId;
    }
  }

  record OverdueCheckoutRegistered(
      UUID eventId, Instant when, UUID patronId, UUID bookId, UUID libraryBranchId)
      implements PatronEvent {

    public OverdueCheckoutRegistered {
      Objects.requireNonNull(eventId, "eventId");
      Objects.requireNonNull(when, "when");
      Objects.requireNonNull(patronId, "patronId");
      Objects.requireNonNull(bookId, "bookId");
      Objects.requireNonNull(libraryBranchId, "libraryBranchId");
    }

    public OverdueCheckoutRegistered(
        Instant when, UUID patronId, UUID bookId, UUID libraryBranchId) {
      this(UUID.randomUUID(), when, patronId, bookId, libraryBranchId);
    }

    public static OverdueCheckoutRegistered registeredAt(
        Instant timestamp, PatronId patronId, BookId bookId, LibraryBranchId libraryBranchId) {
      return new OverdueCheckoutRegistered(
          timestamp,
          patronId.getPatronId(),
          bookId.getBookId(),
          libraryBranchId.getLibraryBranchId());
    }

    @Override
    public UUID getEventId() {
      return eventId;
    }

    @Override
    public Instant getWhen() {
      return when;
    }

    @Override
    public UUID getPatronId() {
      return patronId;
    }

    public UUID getBookId() {
      return bookId;
    }

    public UUID getLibraryBranchId() {
      return libraryBranchId;
    }
  }

  record PatronSuspended(UUID eventId, Instant when, UUID patronId, String reason)
      implements PatronEvent {

    public PatronSuspended {
      Objects.requireNonNull(eventId, "eventId");
      Objects.requireNonNull(when, "when");
      Objects.requireNonNull(patronId, "patronId");
      Objects.requireNonNull(reason, "reason");
    }

    public PatronSuspended(Instant when, UUID patronId, String reason) {
      this(UUID.randomUUID(), when, patronId, reason);
    }

    public static PatronSuspended suspendedAt(
        Instant timestamp, PatronId patronId, String reason) {
      return new PatronSuspended(timestamp, patronId.getPatronId(), reason);
    }

    @Override
    public UUID getEventId() {
      return eventId;
    }

    @Override
    public Instant getWhen() {
      return when;
    }

    @Override
    public UUID getPatronId() {
      return patronId;
    }

    public String getReason() {
      return reason;
    }
  }

  record PatronReactivated(UUID eventId, Instant when, UUID patronId) implements PatronEvent {

    public PatronReactivated {
      Objects.requireNonNull(eventId, "eventId");
      Objects.requireNonNull(when, "when");
      Objects.requireNonNull(patronId, "patronId");
    }

    public PatronReactivated(Instant when, UUID patronId) {
      this(UUID.randomUUID(), when, patronId);
    }

    public static PatronReactivated reactivatedAt(Instant timestamp, PatronId patronId) {
      return new PatronReactivated(timestamp, patronId.getPatronId());
    }

    @Override
    public UUID getEventId() {
      return eventId;
    }

    @Override
    public Instant getWhen() {
      return when;
    }

    @Override
    public UUID getPatronId() {
      return patronId;
    }
  }
}
