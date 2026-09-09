package io.pillopl.library.lending.patron.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;

@Value
class Hold {

    @NonNull BookId bookId;
    @NonNull LibraryBranchId libraryBranchId;
    Instant till;
    int extensionCount;

    Hold(BookId bookId, LibraryBranchId libraryBranchId) {
        this(bookId, libraryBranchId, null, 0);
    }

    Hold(BookId bookId, LibraryBranchId libraryBranchId, Instant till, int extensionCount) {
        if (extensionCount < 0) {
            throw new IllegalArgumentException("Extension count cannot be negative");
        }
        this.bookId = bookId;
        this.libraryBranchId = libraryBranchId;
        this.till = till;
        this.extensionCount = extensionCount;
    }

    boolean matches(@NonNull BookOnHold bookOnHold) {
        return bookId.equals(bookOnHold.getBookId())
                && libraryBranchId.equals(bookOnHold.getHoldPlacedAt());
    }

    boolean isOpenEnded() {
        return till == null;
    }

    boolean isCurrentAt(@NonNull Instant timestamp) {
        return till != null && timestamp.isBefore(till);
    }
}
