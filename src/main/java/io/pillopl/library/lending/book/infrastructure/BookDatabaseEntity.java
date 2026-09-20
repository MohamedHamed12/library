package io.pillopl.library.lending.book.infrastructure;

import static io.pillopl.library.lending.book.infrastructure.BookDatabaseEntity.BookState.*;

import java.time.Instant;
import java.util.UUID;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.PatronReference;
import io.pillopl.library.lending.book.model.*;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
class BookDatabaseEntity {

  enum BookState {
    Available,
    OnHold,
    CheckedOut
  }

  UUID book_id;
  BookType book_type;
  BookState book_state;
  UUID available_at_branch;
  UUID on_hold_at_branch;
  UUID on_hold_by_patron;
  Instant on_hold_till;
  UUID checked_out_at_branch;
  UUID checked_out_by_patron;
  int version;

  Book toDomainModel() {
    return switch (book_state) {
      case Available -> toAvailableBook();
      case OnHold -> toBookOnHold();
      case CheckedOut -> toCheckedOutBook();
    };
  }

  private AvailableBook toAvailableBook() {
    return new AvailableBook(
        new BookId(book_id),
        book_type,
        new LibraryBranchId(available_at_branch),
        new Version(version));
  }

  private BookOnHold toBookOnHold() {
    return new BookOnHold(
        new BookId(book_id),
        book_type,
        new LibraryBranchId(on_hold_at_branch),
        PatronReference.of(on_hold_by_patron),
        on_hold_till,
        new Version(version));
  }

  private CheckedOutBook toCheckedOutBook() {
    return new CheckedOutBook(
        new BookId(book_id),
        book_type,
        new LibraryBranchId(checked_out_at_branch),
        PatronReference.of(checked_out_by_patron),
        new Version(version));
  }
}
