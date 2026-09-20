package io.pillopl.library.lending.patron.application.hold;

import static io.pillopl.library.commons.commands.Result.Rejection;
import static io.pillopl.library.commons.commands.Result.Success;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.book.FindBookOnHold;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.patron.model.Patron;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldCanceled;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldCancelingFailed;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.Patrons;
import io.vavr.control.Either;
import io.vavr.control.Try;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class CancelingHold {

  private final FindBookOnHold findBookOnHold;
  private final Patrons patronRepository;

  public Try<Result> cancelHold(@NonNull CancelHoldCommand command) {
    return Try.of(
        () -> {
          BookOnHold bookOnHold = find(command.getBookId(), command.getPatronId());
          Patron patron = find(command.getPatronId());
          Either<BookHoldCancelingFailed, BookHoldCanceled> result =
              patron.cancelHold(bookOnHold, command.getTimestamp());
          return result.fold(this::publishEvents, this::publishEvents);
        });
  }

  private Result publishEvents(BookHoldCanceled bookHoldCanceled) {
    patronRepository.publish(bookHoldCanceled);
    return Success;
  }

  private Result publishEvents(BookHoldCancelingFailed bookHoldCancelingFailed) {
    patronRepository.publish(bookHoldCancelingFailed);
    return Rejection;
  }

  private BookOnHold find(BookId bookId, PatronId patronId) {
    return findBookOnHold
        .findBookOnHold(bookId, patronId)
        .getOrElseThrow(() -> new HoldNotFoundException(bookId));
  }

  private Patron find(PatronId patronId) {
    return patronRepository
        .findBy(patronId)
        .getOrElseThrow(() -> new PatronNotFoundException(patronId));
  }
}
