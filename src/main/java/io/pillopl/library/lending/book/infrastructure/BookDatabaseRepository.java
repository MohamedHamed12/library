package io.pillopl.library.lending.book.infrastructure;

import static io.pillopl.library.lending.book.infrastructure.BookDatabaseEntity.BookState.Available;
import static io.pillopl.library.lending.book.infrastructure.BookDatabaseEntity.BookState.CheckedOut;
import static io.pillopl.library.lending.book.infrastructure.BookDatabaseEntity.BookState.OnHold;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.AggregateRootIsStale;
import io.pillopl.library.lending.PatronReference;
import io.pillopl.library.lending.book.FindAvailableBook;
import io.pillopl.library.lending.book.FindBookOnHold;
import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.lending.book.model.Book;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.book.model.BookRepository;
import io.pillopl.library.lending.book.model.CheckedOutBook;

class BookDatabaseRepository implements BookRepository, FindAvailableBook, FindBookOnHold {

  private final JdbcTemplate jdbcTemplate;

  BookDatabaseRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Optional<Book> findBy(BookId bookId) {
    return findBookById(bookId).map(BookDatabaseEntity::toDomainModel);
  }

  private Optional<BookDatabaseEntity> findBookById(BookId bookId) {
    try {
      return Optional.ofNullable(
          jdbcTemplate.queryForObject(
              "SELECT b.* FROM book_database_entity b WHERE b.book_id = ?",
              (rs, rowNum) -> BookDatabaseEntity.from(rs),
              bookId.getBookId()));
    } catch (EmptyResultDataAccessException exception) {
      return Optional.empty();
    }
  }

  @Override
  public void save(Book book) {
    findBy(book.bookId())
        .ifPresentOrElse(ignored -> updateOptimistically(book), () -> insertNew(book));
  }

  private int updateOptimistically(Book book) {
    int result =
        switch (book) {
          case AvailableBook availableBook -> update(availableBook);
          case BookOnHold bookOnHold -> update(bookOnHold);
          case CheckedOutBook checkedOutBook -> update(checkedOutBook);
        };
    if (result == 0) {
      throw new AggregateRootIsStale("Someone has updated book in the meantime, book: " + book);
    }
    return result;
  }

  private int update(AvailableBook availableBook) {
    return jdbcTemplate.update(
        "UPDATE book_database_entity SET book_state = ?, available_at_branch = ?, version = ? WHERE book_id = ? AND version = ?",
        Available.toString(),
        availableBook.getLibraryBranch().getLibraryBranchId(),
        availableBook.getVersion().getVersion() + 1,
        availableBook.getBookId().getBookId(),
        availableBook.getVersion().getVersion());
  }

  private int update(BookOnHold bookOnHold) {
    return jdbcTemplate.update(
        "UPDATE book_database_entity SET book_state = ?, on_hold_at_branch = ?, on_hold_by_patron = ?, on_hold_till = ?, version = ? WHERE book_id = ? AND version = ?",
        OnHold.toString(),
        bookOnHold.getHoldPlacedAt().getLibraryBranchId(),
        bookOnHold.getByPatron().getPatronId(),
        toTimestamp(bookOnHold.getHoldTill()),
        bookOnHold.getVersion().getVersion() + 1,
        bookOnHold.getBookId().getBookId(),
        bookOnHold.getVersion().getVersion());
  }

  private int update(CheckedOutBook checkedoutBook) {
    return jdbcTemplate.update(
        "UPDATE book_database_entity SET book_state = ?, checked_out_at_branch = ?, checked_out_by_patron = ?, version = ? WHERE book_id = ? AND version = ?",
        CheckedOut.toString(),
        checkedoutBook.getCheckedOutAt().getLibraryBranchId(),
        checkedoutBook.getByPatron().getPatronId(),
        checkedoutBook.getVersion().getVersion() + 1,
        checkedoutBook.getBookId().getBookId(),
        checkedoutBook.getVersion().getVersion());
  }

  private void insertNew(Book book) {
    switch (book) {
      case AvailableBook availableBook -> insert(availableBook);
      case BookOnHold bookOnHold -> insert(bookOnHold);
      case CheckedOutBook checkedOutBook -> insert(checkedOutBook);
    }
  }

  private int insert(AvailableBook availableBook) {
    return insert(
        availableBook.getBookId(),
        availableBook.type(),
        Available,
        availableBook.getLibraryBranch().getLibraryBranchId(),
        null,
        null,
        null,
        null,
        null);
  }

  private int insert(BookOnHold bookOnHold) {
    return insert(
        bookOnHold.getBookId(),
        bookOnHold.type(),
        OnHold,
        null,
        bookOnHold.getHoldPlacedAt().getLibraryBranchId(),
        bookOnHold.getByPatron().getPatronId(),
        bookOnHold.getHoldTill(),
        null,
        null);
  }

  private int insert(CheckedOutBook checkedoutBook) {
    return insert(
        checkedoutBook.getBookId(),
        checkedoutBook.type(),
        CheckedOut,
        null,
        null,
        null,
        null,
        checkedoutBook.getCheckedOutAt().getLibraryBranchId(),
        checkedoutBook.getByPatron().getPatronId());
  }

  private int insert(
      BookId bookId,
      BookType bookType,
      BookDatabaseEntity.BookState state,
      UUID availableAt,
      UUID onHoldAt,
      UUID onHoldBy,
      Instant onHoldTill,
      UUID checkedOutAt,
      UUID checkedOutBy) {
    return jdbcTemplate.update(
        """
        INSERT INTO book_database_entity (
            book_id,
            book_type,
            book_state,
            available_at_branch,
            on_hold_at_branch,
            on_hold_by_patron,
            on_hold_till,
            checked_out_at_branch,
            checked_out_by_patron,
            version
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
        """,
        bookId.getBookId(),
        bookType.toString(),
        state.toString(),
        availableAt,
        onHoldAt,
        onHoldBy,
        toTimestamp(onHoldTill),
        checkedOutAt,
        checkedOutBy);
  }

  private static Timestamp toTimestamp(Instant instant) {
    return instant == null ? null : Timestamp.from(instant);
  }

  @Override
  public Optional<AvailableBook> findAvailableBookBy(BookId bookId) {
    return findBy(bookId).filter(AvailableBook.class::isInstance).map(AvailableBook.class::cast);
  }

  @Override
  public Optional<BookOnHold> findBookOnHold(BookId bookId, PatronReference patronId) {
    return findBy(bookId).filter(BookOnHold.class::isInstance).map(BookOnHold.class::cast);
  }
}
