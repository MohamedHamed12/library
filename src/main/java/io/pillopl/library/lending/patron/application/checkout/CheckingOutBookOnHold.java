package io.pillopl.library.lending.patron.application.checkout;

import static io.pillopl.library.commons.commands.Result.Rejection;
import static io.pillopl.library.commons.commands.Result.Success;

import java.util.Objects;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.commands.Decision;
import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.book.FindBookOnHold;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.patron.model.Patron;
import io.pillopl.library.lending.patron.model.PatronEvent.BookCheckedOut;
import io.pillopl.library.lending.patron.model.PatronEvent.BookCheckingOutFailed;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.Patrons;

public class CheckingOutBookOnHold {

  private final FindBookOnHold findBookOnHold;
  private final Patrons patronRepository;

  public CheckingOutBookOnHold(FindBookOnHold findBookOnHold, Patrons patronRepository) {
    this.findBookOnHold = findBookOnHold;
    this.patronRepository = patronRepository;
  }

  public Result checkOut(CheckOutBookCommand command) {
    Objects.requireNonNull(command, "command");
    BookOnHold bookOnHold = find(command.getBookId(), command.getPatronId());
    Patron patron = find(command.getPatronId());
    Decision<BookCheckingOutFailed, BookCheckedOut> result =
        patron.checkOut(bookOnHold, command.getCheckoutDuration(), command.getTimestamp());
    return result.fold(this::publishEvents, this::publishEvents);
  }

  private Result publishEvents(BookCheckedOut bookCheckedOut) {
    patronRepository.publish(bookCheckedOut);
    return Success;
  }

  private Result publishEvents(BookCheckingOutFailed bookCheckingOutFailed) {
    patronRepository.publish(bookCheckingOutFailed);
    return Rejection;
  }

  private BookOnHold find(BookId id, PatronId patronId) {
    return findBookOnHold
        .findBookOnHold(id, patronId)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Cannot find book on hold with Id: " + id.getBookId()));
  }

  private Patron find(PatronId patronId) {
    return patronRepository
        .findBy(patronId)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Patron with given Id does not exists: " + patronId.getPatronId()));
  }
}
