package io.pillopl.library.lending.patron.application.hold;

import static io.pillopl.library.commons.commands.Result.Rejection;
import static io.pillopl.library.commons.commands.Result.Success;

import java.util.Objects;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.commands.Decision;
import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.book.FindBookOnHold;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.patron.model.Patron;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldCanceled;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldCancelingFailed;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.Patrons;

public class CancelingHold {

  private final FindBookOnHold findBookOnHold;
  private final Patrons patronRepository;

  public CancelingHold(FindBookOnHold findBookOnHold, Patrons patronRepository) {
    this.findBookOnHold = findBookOnHold;
    this.patronRepository = patronRepository;
  }

  public Result cancelHold(CancelHoldCommand command) {
    Objects.requireNonNull(command, "command");
    BookOnHold bookOnHold = find(command.getBookId(), command.getPatronId());
    Patron patron = find(command.getPatronId());
    Decision<BookHoldCancelingFailed, BookHoldCanceled> result =
        patron.cancelHold(bookOnHold, command.getTimestamp());
    return result.fold(this::publishEvents, this::publishEvents);
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
        .orElseThrow(() -> new HoldNotFoundException(bookId));
  }

  private Patron find(PatronId patronId) {
    return patronRepository.findBy(patronId).orElseThrow(() -> new PatronNotFoundException(patronId));
  }
}
