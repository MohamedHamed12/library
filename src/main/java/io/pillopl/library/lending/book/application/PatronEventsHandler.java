package io.pillopl.library.lending.book.application;

import java.time.Clock;

import org.springframework.context.event.EventListener;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.LendingEvent.BookCheckedOutEvent;
import io.pillopl.library.lending.LendingEvent.BookHoldCanceledEvent;
import io.pillopl.library.lending.LendingEvent.BookHoldExpiredEvent;
import io.pillopl.library.lending.LendingEvent.BookHoldExtendedEvent;
import io.pillopl.library.lending.LendingEvent.BookPlacedOnHoldEvent;
import io.pillopl.library.lending.LendingEvent.BookReturnedEvent;
import io.pillopl.library.lending.PatronReference;
import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.lending.book.model.Book;
import io.pillopl.library.lending.book.model.BookDuplicateHoldFound;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.book.model.BookRepository;
import io.pillopl.library.lending.book.model.CheckedOutBook;

public class PatronEventsHandler {

  private final BookRepository bookRepository;
  private final DomainEvents domainEvents;
  private final Clock clock;

  public PatronEventsHandler(
      BookRepository bookRepository, DomainEvents domainEvents, Clock clock) {
    this.bookRepository = bookRepository;
    this.domainEvents = domainEvents;
    this.clock = clock;
  }

  @EventListener
  void handle(BookPlacedOnHoldEvent bookPlacedOnHold) {
    bookRepository
        .findBy(new BookId(bookPlacedOnHold.getBookId()))
        .map(book -> handleBookPlacedOnHold(book, bookPlacedOnHold))
        .map(this::saveBook);
  }

  @EventListener
  void handle(BookHoldExtendedEvent bookHoldExtended) {
    bookRepository
        .findBy(new BookId(bookHoldExtended.getBookId()))
        .map(book -> handleBookHoldExtended(book, bookHoldExtended))
        .map(this::saveBook);
  }

  @EventListener
  void handle(BookCheckedOutEvent bookCheckedOut) {
    bookRepository
        .findBy(new BookId(bookCheckedOut.getBookId()))
        .map(book -> handleBookCheckedOut(book, bookCheckedOut))
        .map(this::saveBook);
  }

  @EventListener
  void handle(BookHoldExpiredEvent holdExpired) {
    bookRepository
        .findBy(new BookId(holdExpired.getBookId()))
        .map(book -> handleBookHoldExpired(book, holdExpired))
        .map(this::saveBook);
  }

  @EventListener
  void handle(BookHoldCanceledEvent holdCanceled) {
    bookRepository
        .findBy(new BookId(holdCanceled.getBookId()))
        .map(book -> handleBookHoldCanceled(book, holdCanceled))
        .map(this::saveBook);
  }

  @EventListener
  void handle(BookReturnedEvent bookReturned) {
    bookRepository
        .findBy(new BookId(bookReturned.getBookId()))
        .map(book -> handleBookReturned(book, bookReturned))
        .map(this::saveBook);
  }

  private Book handleBookPlacedOnHold(Book book, BookPlacedOnHoldEvent event) {
    return switch (book) {
      case AvailableBook availableBook -> availableBook.handle(event);
      case BookOnHold bookOnHold -> raiseDuplicateHoldFoundEvent(bookOnHold, event);
      case CheckedOutBook checkedOutBook -> checkedOutBook;
    };
  }

  private Book handleBookHoldExtended(Book book, BookHoldExtendedEvent event) {
    return switch (book) {
      case BookOnHold onHold -> onHold.handle(event);
      case AvailableBook availableBook -> availableBook;
      case CheckedOutBook checkedOutBook -> checkedOutBook;
    };
  }

  private BookOnHold raiseDuplicateHoldFoundEvent(
      BookOnHold onHold, BookPlacedOnHoldEvent bookPlacedOnHold) {
    if (onHold.by(PatronReference.of(bookPlacedOnHold.getPatronId()))) {
      return onHold;
    }
    domainEvents.publish(
        new BookDuplicateHoldFound(
            clock.instant(),
            onHold.getByPatron().getPatronId(),
            bookPlacedOnHold.getPatronId(),
            bookPlacedOnHold.getLibraryBranchId(),
            bookPlacedOnHold.getBookId()));
    return onHold;
  }

  private Book handleBookHoldExpired(Book book, BookHoldExpiredEvent event) {
    return switch (book) {
      case BookOnHold onHold -> onHold.handle(event);
      case AvailableBook availableBook -> availableBook;
      case CheckedOutBook checkedOutBook -> checkedOutBook;
    };
  }

  private Book handleBookHoldCanceled(Book book, BookHoldCanceledEvent event) {
    return switch (book) {
      case BookOnHold onHold -> onHold.handle(event);
      case AvailableBook availableBook -> availableBook;
      case CheckedOutBook checkedOutBook -> checkedOutBook;
    };
  }

  private Book handleBookCheckedOut(Book book, BookCheckedOutEvent event) {
    return switch (book) {
      case BookOnHold onHold -> onHold.handle(event);
      case AvailableBook availableBook -> availableBook;
      case CheckedOutBook checkedOutBook -> checkedOutBook;
    };
  }

  private Book handleBookReturned(Book book, BookReturnedEvent event) {
    return switch (book) {
      case CheckedOutBook checkedOut -> checkedOut.handle(event);
      case AvailableBook availableBook -> availableBook;
      case BookOnHold bookOnHold -> bookOnHold;
    };
  }

  private Book saveBook(Book book) {
    bookRepository.save(book);
    return book;
  }
}
