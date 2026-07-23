package io.pillopl.library.lending.patron.application.hold;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.HoldDuration;
import io.pillopl.library.lending.patron.model.NumberOfDays;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Option;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;

@Value
public class PlaceOnHoldCommand {

    @NonNull
    Instant timestamp;

    @NonNull
    PatronId patronId;

    @NonNull
    LibraryBranchId libraryId;

    @NonNull
    BookId bookId;

    Option<Integer> noOfDays;

    static PlaceOnHoldCommand closeEnded(
            Instant timestamp,
            PatronId patronId,
            LibraryBranchId libraryBranchId,
            BookId bookId,
            int forDays
    ) {
        return new PlaceOnHoldCommand(
                timestamp,
                patronId,
                libraryBranchId,
                bookId,
                Option.of(forDays)
        );
    }

    static PlaceOnHoldCommand openEnded(
            Instant timestamp,
            PatronId patronId,
            LibraryBranchId libraryBranchId,
            BookId bookId
    ) {
        return new PlaceOnHoldCommand(
                timestamp,
                patronId,
                libraryBranchId,
                bookId,
                Option.none()
        );
    }

    HoldDuration getHoldDuration() {
        return noOfDays
                .map(NumberOfDays::of)
                .map(days -> HoldDuration.closeEnded(timestamp, days))
                .getOrElse(() -> HoldDuration.openEnded(timestamp));
    }
}