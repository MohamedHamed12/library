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
import java.time.Instant;

import static io.pillopl.library.commons.events.EitherResult.announceFailure;
import static io.pillopl.library.commons.events.EitherResult.announceSuccess;
import static io.pillopl.library.lending.patron.model.PatronEvent.BookHoldCanceled.canceledAt;
import static io.pillopl.library.lending.patron.model.PatronEvent.BookHoldCancelingFailed.cancellationFailedAt;
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

    public Either<BookHoldFailed, BookPlacedOnHoldEvents> placeOnHold(
            AvailableBook book,
            HoldDuration duration,
            Instant timestamp) {
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

    int overdueCheckoutsAt(LibraryBranchId libraryBranch) {
        return overdueCheckouts.countAt(libraryBranch);
    }

    public int numberOfHolds() {
        return patronHolds.count();
    }

}
