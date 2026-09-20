package io.pillopl.library.lending.book.application;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;

import java.time.Clock;

import org.springframework.context.event.EventListener;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.LendingEvent.*;
import io.pillopl.library.lending.PatronReference;
import io.pillopl.library.lending.book.model.*;
import io.vavr.API;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PatronEventsHandler {

  private final BookRepository bookRepository;
  private final DomainEvents domainEvents;
  private final Clock clock;

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

  private Book handleBookPlacedOnHold(Book book, BookPlacedOnHoldEvent bookPlacedOnHold) {
    return API.Match(book)
        .of(
            Case(
                $(instanceOf(AvailableBook.class)),
                availableBook -> availableBook.handle(bookPlacedOnHold)),
            Case(
                $(instanceOf(BookOnHold.class)),
                bookOnHold -> raiseDuplicateHoldFoundEvent(bookOnHold, bookPlacedOnHold)),
            Case($(), () -> book));
  }

  private Book handleBookHoldExtended(Book book, BookHoldExtendedEvent bookHoldExtended) {
    return API.Match(book)
        .of(
            Case($(instanceOf(BookOnHold.class)), onHold -> onHold.handle(bookHoldExtended)),
            Case($(), () -> book));
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

  private Book handleBookHoldExpired(Book book, BookHoldExpiredEvent holdExpired) {
    return API.Match(book)
        .of(
            Case($(instanceOf(BookOnHold.class)), onHold -> onHold.handle(holdExpired)),
            Case($(), () -> book));
  }

  private Book handleBookHoldCanceled(Book book, BookHoldCanceledEvent holdCanceled) {
    return API.Match(book)
        .of(
            Case($(instanceOf(BookOnHold.class)), onHold -> onHold.handle(holdCanceled)),
            Case($(), () -> book));
  }

  private Book handleBookCheckedOut(Book book, BookCheckedOutEvent bookCheckedOut) {
    return API.Match(book)
        .of(
            Case($(instanceOf(BookOnHold.class)), onHold -> onHold.handle(bookCheckedOut)),
            Case($(), () -> book));
  }

  private Book handleBookReturned(Book book, BookReturnedEvent bookReturned) {
    return API.Match(book)
        .of(
            Case(
                $(instanceOf(CheckedOutBook.class)), checkedOut -> checkedOut.handle(bookReturned)),
            Case($(), () -> book));
  }

  private Book saveBook(Book book) {
    bookRepository.save(book);
    return book;
  }
}
