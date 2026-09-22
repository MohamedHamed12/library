package io.pillopl.library.lending.book.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.aggregates.AggregateRootIsStale;
import io.pillopl.library.lending.PatronReference;
import io.pillopl.library.lending.book.FindAvailableBook;
import io.pillopl.library.lending.book.FindBookOnHold;
import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.lending.book.model.Book;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.book.model.BookRepository;

class BookDatabaseRepository implements BookRepository, FindAvailableBook, FindBookOnHold {

  private final BookJpaRepository repository;

  BookDatabaseRepository(BookJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Book> findBy(BookId bookId) {
    return repository.findByBookId(bookId.getBookId()).map(BookDatabaseEntity::toDomainModel);
  }

  @Override
  @Transactional
  public void save(Book book) {
    Optional<BookDatabaseEntity> persisted = repository.findByBookId(book.bookId().getBookId());
    if (persisted.isEmpty()) {
      repository.saveAndFlush(BookDatabaseEntity.from(book));
      return;
    }

    BookDatabaseEntity entity = persisted.orElseThrow();
    if (entity.getVersion() != book.getVersion().getVersion()) {
      throw stale(book);
    }

    entity.apply(book);
    try {
      repository.saveAndFlush(entity);
    } catch (OptimisticLockingFailureException exception) {
      throw stale(book);
    }
  }

  private AggregateRootIsStale stale(Book book) {
    return new AggregateRootIsStale("Someone has updated book in the meantime, book: " + book);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<AvailableBook> findAvailableBookBy(BookId bookId) {
    return findBy(bookId).filter(AvailableBook.class::isInstance).map(AvailableBook.class::cast);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<BookOnHold> findBookOnHold(BookId bookId, PatronReference patronId) {
    return findBy(bookId).filter(BookOnHold.class::isInstance).map(BookOnHold.class::cast);
  }
}

interface BookJpaRepository extends JpaRepository<BookDatabaseEntity, Integer> {

  Optional<BookDatabaseEntity> findByBookId(UUID bookId);
}
