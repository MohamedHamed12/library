package io.pillopl.library.lending.book.infrastructure;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.lending.PatronReference;
import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.lending.book.model.Book;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.book.model.CheckedOutBook;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;

@Entity
@Table(name = "book_database_entity")
class BookDatabaseEntity {

  enum BookState {
    Available,
    OnHold,
    CheckedOut
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "book_id", nullable = false, unique = true)
  private UUID bookId;

  @Enumerated(EnumType.STRING)
  @Column(name = "book_type", nullable = false, length = 100)
  private BookType bookType;

  @Enumerated(EnumType.STRING)
  @Column(name = "book_state", nullable = false, length = 100)
  private BookState bookState;

  @Column(name = "available_at_branch")
  private UUID availableAtBranch;

  @Column(name = "on_hold_at_branch")
  private UUID onHoldAtBranch;

  @Column(name = "on_hold_by_patron")
  private UUID onHoldByPatron;

  @Column(name = "on_hold_till")
  private Instant onHoldTill;

  @Column(name = "checked_out_at_branch")
  private UUID checkedOutAtBranch;

  @Column(name = "checked_out_by_patron")
  private UUID checkedOutByPatron;

  @Version
  @Column(name = "version")
  private int version;

  protected BookDatabaseEntity() {}

  private BookDatabaseEntity(Book book) {
    bookId = book.bookId().getBookId();
    bookType = book.type();
    version = book.getVersion().getVersion();
    applyState(book);
  }

  static BookDatabaseEntity from(Book book) {
    return new BookDatabaseEntity(book);
  }

  int getVersion() {
    return version;
  }

  void apply(Book book) {
    if (!bookId.equals(book.bookId().getBookId())) {
      throw new IllegalArgumentException("Cannot apply a different book to persisted entity");
    }
    bookType = book.type();
    applyState(book);
  }

  private void applyState(Book book) {
    switch (book) {
      case AvailableBook availableBook -> {
        bookState = BookState.Available;
        availableAtBranch = availableBook.getLibraryBranch().getLibraryBranchId();
      }
      case BookOnHold bookOnHold -> {
        bookState = BookState.OnHold;
        onHoldAtBranch = bookOnHold.getHoldPlacedAt().getLibraryBranchId();
        onHoldByPatron = bookOnHold.getByPatron().getPatronId();
        onHoldTill = bookOnHold.getHoldTill();
      }
      case CheckedOutBook checkedOutBook -> {
        bookState = BookState.CheckedOut;
        checkedOutAtBranch = checkedOutBook.getCheckedOutAt().getLibraryBranchId();
        checkedOutByPatron = checkedOutBook.getByPatron().getPatronId();
      }
    }
  }

  Book toDomainModel() {
    return switch (bookState) {
      case Available ->
          new AvailableBook(
              new BookId(bookId),
              bookType,
              new LibraryBranchId(availableAtBranch),
              new io.pillopl.library.commons.aggregates.Version(version));
      case OnHold ->
          new BookOnHold(
              new BookId(bookId),
              bookType,
              new LibraryBranchId(onHoldAtBranch),
              PatronReference.of(onHoldByPatron),
              onHoldTill,
              new io.pillopl.library.commons.aggregates.Version(version));
      case CheckedOut ->
          new CheckedOutBook(
              new BookId(bookId),
              bookType,
              new LibraryBranchId(checkedOutAtBranch),
              PatronReference.of(checkedOutByPatron),
              new io.pillopl.library.commons.aggregates.Version(version));
    };
  }
}
