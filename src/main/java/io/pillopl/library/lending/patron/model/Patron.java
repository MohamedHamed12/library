package io.pillopl.library.lending.patron.model;

import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent.*;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.time.Duration;
import java.time.Instant;

import static io.pillopl.library.commons.events.EitherResult.announceFailure;
import static io.pillopl.library.commons.events.EitherResult.announceSuccess;
import static io.pillopl.library.lending.patron.model.PatronEvent.BookHoldCanceled.canceledAt;
import static io.pillopl.library.lending.patron.model.PatronEvent.BookHoldCancelingFailed.cancellationFailedAt;
import static io.pillopl.library.lending.patron.model.PatronEvent.BookHoldExtended.extendedAt;
import static io.pillopl.library.lending.patron.model.PatronEvent.BookHoldExtensionFailed.extensionFailedAt;
import static io.pillopl.library.lending.patron.model.PatronEvent.BookHoldFailed.holdFailedAt;
import static io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHold.placedOnHoldAt;
import static io.pillopl.library.lending.patron.model.PatronEvent.BookCheckedOut.checkedOutAt;
import static io.pillopl.library.lending.patron.model.PatronEvent.BookCheckingOutFailed.checkoutFailedAt;
import static io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHoldEvents.events;
import static io.pillopl.library.lending.patron.model.PatronHolds.MAX_NUMBER_OF_HOLDS;
import static io.pillopl.library.lending.patron.model.Rejection.withReason;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "patron")
public class Patron {

    @NonNull
    private final PatronInformation patron;

    @NonNull
    private final List<PlacingOnHoldPolicy> placingOnHoldPolicies;

    @NonNull
    private final OverdueCheckouts overdueCheckouts;

    @NonNull
    private final PatronHolds patronHolds;

    private final PatronStatus status;
    private final String suspensionReason;

    public Either<Rejection, PatronSuspended> suspend(String reason, Instant timestamp) {
        if (status == PatronStatus.SUSPENDED) {
            return announceFailure(withReason("Patron is already suspended"));
        }

        if (reason == null || reason.trim().isEmpty()) {
            return announceFailure(withReason("Suspension reason must not be blank"));
        }

        return announceSuccess(PatronSuspended.suspendedAt(timestamp, patron.getPatronId(), reason));
    }

    public Either<Rejection, PatronReactivated> reactivate(Instant timestamp) {
        if (status == PatronStatus.ACTIVE) {
            return announceFailure(withReason("Patron is already active"));
        }

        return announceSuccess(PatronReactivated.reactivatedAt(timestamp, patron.getPatronId()));
    }

    public Either<BookHoldFailed, BookPlacedOnHoldEvents> placeOnHold(
            AvailableBook book,
            HoldDuration duration,
            Instant timestamp) {
        if (isSuspended()) {
            return announceFailure(holdFailedAt(timestamp, withReason("Patron is suspended"), book.getBookId(), book.getLibraryBranch(), patron));
        }

        Option<Rejection> rejection = patronCanHold(book, duration);
        if (rejection.isEmpty()) {
            BookPlacedOnHold bookPlacedOnHold = placedOnHoldAt(timestamp, book.getBookId(), book.type(),
                    book.getLibraryBranch(), patron.getPatronId(), duration);
            if (patronHolds.maximumHoldsAfterHolding(book)) {
                return announceSuccess(
                        events(bookPlacedOnHold, MaximumNumberOhHoldsReached.reachedAt(timestamp, patron, MAX_NUMBER_OF_HOLDS)));
            }
            return announceSuccess(events(bookPlacedOnHold));
        }
        return announceFailure(holdFailedAt(timestamp, rejection.get(), book.getBookId(), book.getLibraryBranch(), patron));
    }

    public Either<BookHoldExtensionFailed, BookHoldExtended> extendHold(
            BookOnHold book,
            NumberOfDays additionalDays,
            Instant timestamp) {
        Option<Hold> hold = patronHolds.find(book);
        if (hold.isEmpty()) {
            return extensionFailure(timestamp, book, "book is not on hold by patron");
        }

        Hold currentHold = hold.get();
        if (currentHold.isOpenEnded()) {
            return extensionFailure(timestamp, book, "open-ended hold cannot be extended");
        }
        if (!currentHold.isCurrentAt(timestamp)) {
            return extensionFailure(timestamp, book, "expired hold cannot be extended");
        }
        if (additionalDays.isGreaterThan(maxExtensionDays())) {
            return extensionFailure(timestamp, book, "hold extension exceeds maximum number of days");
        }
        if (currentHold.getExtensionCount() >= maxNumberOfExtensions()) {
            return extensionFailure(timestamp, book, "hold extension limit has been reached");
        }

        Instant newTill = currentHold.getTill().plus(Duration.ofDays(additionalDays.getDays()));
        return announceSuccess(extendedAt(
                timestamp,
                book.getBookId(),
                book.getHoldPlacedAt(),
                patron.getPatronId(),
                currentHold.getTill(),
                newTill,
                currentHold.getExtensionCount() + 1));
    }

    private Either<BookHoldExtensionFailed, BookHoldExtended> extensionFailure(
            Instant timestamp,
            BookOnHold book,
            String reason) {
        return announceFailure(extensionFailedAt(
                timestamp,
                withReason(reason),
                book.getBookId(),
                book.getHoldPlacedAt(),
                patron));
    }

    private int maxExtensionDays() {
        return isRegular() ? 7 : 14;
    }

    private int maxNumberOfExtensions() {
        return isRegular() ? 1 : 2;
    }

    public Either<BookHoldCancelingFailed, BookHoldCanceled> cancelHold(
            BookOnHold book,
            Instant timestamp) {
        if (patronHolds.a(book)) {
            return announceSuccess(
                    canceledAt(
                            timestamp,
                            book.getBookId(),
                            book.getHoldPlacedAt(),
                            patron.getPatronId()));
        }

        return announceFailure(
                cancellationFailedAt(
                        timestamp,
                        book.getBookId(),
                        book.getHoldPlacedAt(),
                        patron.getPatronId()));
    }

    public Either<BookCheckingOutFailed, BookCheckedOut> checkOut(BookOnHold book, CheckoutDuration duration, Instant timestamp) {
        if (isSuspended()) {
            return announceFailure(checkoutFailedAt(timestamp, withReason("Patron is suspended"), book.getBookId(),
                    book.getHoldPlacedAt(), patron));
        }

        if (patronHolds.a(book)) {
            return announceSuccess(checkedOutAt(timestamp, book.getBookId(), book.type(), book.getHoldPlacedAt(),
                    patron.getPatronId(), duration));
        }
        return announceFailure(checkoutFailedAt(timestamp, withReason("book is not on hold by patron"), book.getBookId(),
                book.getHoldPlacedAt(), patron));
    }

    private Option<Rejection> patronCanHold(AvailableBook aBook, HoldDuration forDuration) {
        return placingOnHoldPolicies
                .toStream()
                .map(policy -> policy.apply(aBook, this, forDuration))
                .find(Either::isLeft)
                .map(Either::getLeft);
    }

    boolean isRegular() {
        return patron.isRegular();
    }

    private boolean isSuspended() {
        return status == PatronStatus.SUSPENDED;
    }

    int overdueCheckoutsAt(LibraryBranchId libraryBranch) {
        return overdueCheckouts.countAt(libraryBranch);
    }

    public int numberOfHolds() {
        return patronHolds.count();
    }
}
