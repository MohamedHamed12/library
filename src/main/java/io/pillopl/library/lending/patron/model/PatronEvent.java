package io.pillopl.library.lending.patron.model;

import io.pillopl.library.commons.events.DomainEvent;
import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.vavr.collection.List;
import io.vavr.control.Option;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

public interface PatronEvent extends DomainEvent {

    default PatronId patronId() {
        return new PatronId(getPatronId());
    }

    UUID getPatronId();

    default UUID getAggregateId() {
        return getPatronId();
    }

    default List<DomainEvent> normalize() {
        return List.of(this);
    }

    @Value
    class PatronCreated implements PatronEvent {
        @NonNull
        UUID eventId = UUID.randomUUID();
        @NonNull
        Instant when;
        @NonNull
        UUID patronId;
        @NonNull
        PatronType patronType;

        public static PatronCreated createdAt(Instant timestamp, PatronId patronId, PatronType type) {
            return new PatronCreated(timestamp, patronId.getPatronId(), type);
        }
    }

    @Value
    class BookPlacedOnHold implements PatronEvent {
        @NonNull
        UUID eventId = UUID.randomUUID();
        @NonNull
        Instant when;
        @NonNull
        UUID patronId;
        @NonNull
        UUID bookId;
        @NonNull
        BookType bookType;
        @NonNull
        UUID libraryBranchId;
        @NonNull
        Instant holdFrom;
        Instant holdTill;

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
    }

    @Value
    class BookPlacedOnHoldEvents implements PatronEvent {
        @NonNull
        UUID eventId = UUID.randomUUID();
        @NonNull
        UUID patronId;
        @NonNull
        BookPlacedOnHold bookPlacedOnHold;
        @NonNull
        Option<MaximumNumberOhHoldsReached> maximumNumberOhHoldsReached;

        @Override
        public Instant getWhen() {
            return bookPlacedOnHold.when;
        }

        public static BookPlacedOnHoldEvents events(BookPlacedOnHold bookPlacedOnHold) {
            return new BookPlacedOnHoldEvents(bookPlacedOnHold.getPatronId(), bookPlacedOnHold, Option.none());
        }

        public static BookPlacedOnHoldEvents events(BookPlacedOnHold bookPlacedOnHold,
                MaximumNumberOhHoldsReached maximumNumberOhHoldsReached) {
            return new BookPlacedOnHoldEvents(bookPlacedOnHold.patronId, bookPlacedOnHold,
                    Option.of(maximumNumberOhHoldsReached));
        }

        public List<DomainEvent> normalize() {
            return List.<DomainEvent>of(bookPlacedOnHold).appendAll(maximumNumberOhHoldsReached.toList());
        }
    }

    @Value
    class MaximumNumberOhHoldsReached implements PatronEvent {
        @NonNull
        UUID eventId = UUID.randomUUID();
        @NonNull
        Instant when;
        @NonNull
        UUID patronId;
        int numberOfHolds;

        public static MaximumNumberOhHoldsReached reachedAt(
                Instant timestamp,
                PatronInformation patronInformation,
                int numberOfHolds) {
            return new MaximumNumberOhHoldsReached(
                    timestamp,
                    patronInformation.getPatronId().getPatronId(),
                    numberOfHolds);
        }
    }

    @Value
    class BookCheckedOut implements PatronEvent {
        @NonNull
        UUID eventId = UUID.randomUUID();
        @NonNull
        Instant when;
        @NonNull
        UUID patronId;
        @NonNull
        UUID bookId;
        @NonNull
        BookType bookType;
        @NonNull
        UUID libraryBranchId;
        @NonNull
        Instant till;

        public static BookCheckedOut checkedOutAt(Instant timestamp, BookId bookId, BookType bookType,
                LibraryBranchId libraryBranchId, PatronId patronId, CheckoutDuration checkoutDuration) {
            return new BookCheckedOut(
                    timestamp,
                    patronId.getPatronId(),
                    bookId.getBookId(),
                    bookType,
                    libraryBranchId.getLibraryBranchId(),
                    checkoutDuration.to());
        }
    }

    @Value
    class BookReturned implements PatronEvent {
        @NonNull
        UUID eventId = UUID.randomUUID();
        @NonNull
        Instant when;
        @NonNull
        UUID patronId;
        @NonNull
        UUID bookId;
        @NonNull
        BookType bookType;
        @NonNull
        UUID libraryBranchId;
    }

    @Value
    class BookHoldFailed implements PatronEvent {
        @NonNull
        UUID eventId = UUID.randomUUID();
        @NonNull
        String reason;
        @NonNull
        Instant when;
        @NonNull
        UUID patronId;
        @NonNull
        UUID bookId;
        @NonNull
        UUID libraryBranchId;

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
    }

    @Value
    class BookCheckingOutFailed implements PatronEvent {
        @NonNull
        UUID eventId = UUID.randomUUID();
        @NonNull
        String reason;
        @NonNull
        Instant when;
        @NonNull
        UUID patronId;
        @NonNull
        UUID bookId;
        @NonNull
        UUID libraryBranchId;

        static BookCheckingOutFailed checkoutFailedAt(Instant timestamp, Rejection rejection, BookId bookId,
                LibraryBranchId libraryBranchId, PatronInformation patronInformation) {
            return new BookCheckingOutFailed(
                    rejection.getReason().getReason(),
                    timestamp,
                    patronInformation.getPatronId().getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

    @Value
    class BookHoldCanceled implements PatronEvent {
        @NonNull
        UUID eventId = UUID.randomUUID();
        @NonNull
        Instant when;
        @NonNull
        UUID patronId;
        @NonNull
        UUID bookId;
        @NonNull
        UUID libraryBranchId;

        public static BookHoldCanceled canceledAt(
                Instant timestamp,
                BookId bookId,
                LibraryBranchId libraryBranchId,
                PatronId patronId) {
            return new BookHoldCanceled(
                    timestamp,
                    patronId.getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

    @Value
    class BookHoldCancelingFailed implements PatronEvent {
        @NonNull
        UUID eventId = UUID.randomUUID();
        @NonNull
        Instant when;
        @NonNull
        UUID patronId;
        @NonNull
        UUID bookId;
        @NonNull
        UUID libraryBranchId;

        static BookHoldCancelingFailed cancellationFailedAt(
                Instant timestamp,
                BookId bookId,
                LibraryBranchId libraryBranchId,
                PatronId patronId) {
            return new BookHoldCancelingFailed(
                    timestamp,
                    patronId.getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

    @Value
    class BookHoldExpired implements PatronEvent {
        @NonNull
        UUID eventId = UUID.randomUUID();
        @NonNull
        Instant when;
        @NonNull
        UUID patronId;
        @NonNull
        UUID bookId;
        @NonNull
        UUID libraryBranchId;

        public static BookHoldExpired expiredAt(Instant timestamp, BookId bookId, PatronId patronId, LibraryBranchId libraryBranchId) {
            return new BookHoldExpired(
                    timestamp,
                    patronId.getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

    @Value
    class OverdueCheckoutRegistered implements PatronEvent {
        @NonNull
        UUID eventId = UUID.randomUUID();
        @NonNull
        Instant when;
        @NonNull
        UUID patronId;
        @NonNull
        UUID bookId;
        @NonNull
        UUID libraryBranchId;

        public static OverdueCheckoutRegistered registeredAt(Instant timestamp, PatronId patronId, BookId bookId, LibraryBranchId libraryBranchId) {
            return new OverdueCheckoutRegistered(
                    timestamp,
                    patronId.getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

}
