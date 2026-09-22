package io.pillopl.library.lending.book.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.PatronReference;
import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.lending.book.model.Book;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.book.model.CheckedOutBook;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;

record BookDatabaseEntity(
    UUID book_id,
    BookType book_type,
    BookState book_state,
    UUID available_at_branch,
    UUID on_hold_at_branch,
    UUID on_hold_by_patron,
    Instant on_hold_till,
    UUID checked_out_at_branch,
    UUID checked_out_by_patron,
    int version) {

  enum BookState {
    Available,
    OnHold,
    CheckedOut
  }

  static BookDatabaseEntity from(ResultSet rs) throws SQLException {
    Timestamp onHoldTill = rs.getTimestamp("on_hold_till");
    return new BookDatabaseEntity(
        rs.getObject("book_id", UUID.class),
        BookType.valueOf(rs.getString("book_type")),
        BookState.valueOf(rs.getString("book_state")),
        rs.getObject("available_at_branch", UUID.class),
        rs.getObject("on_hold_at_branch", UUID.class),
        rs.getObject("on_hold_by_patron", UUID.class),
        onHoldTill == null ? null : onHoldTill.toInstant(),
        rs.getObject("checked_out_at_branch", UUID.class),
        rs.getObject("checked_out_by_patron", UUID.class),
        rs.getInt("version"));
  }

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
