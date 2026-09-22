package io.pillopl.library.lending.patron.application.hold;

import static io.pillopl.library.commons.commands.Result.Success;

import java.util.Objects;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.commands.Decision;
import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.book.FindAvailableBook;
import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.lending.patron.model.Patron;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldFailed;
import io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHoldEvents;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.Patrons;

public class PlacingOnHold {

  private final FindAvailableBook findAvailableBook;
  private final Patrons patronRepository;

  public PlacingOnHold(FindAvailableBook findAvailableBook, Patrons patronRepository) {
    this.findAvailableBook = findAvailableBook;
    this.patronRepository = patronRepository;
  }

  public Result placeOnHold(PlaceOnHoldCommand command) {
    Objects.requireNonNull(command, "command");
    AvailableBook availableBook = find(command.getBookId());
    Patron patron = find(command.getPatronId());
    Decision<BookHoldFailed, BookPlacedOnHoldEvents> result =
        patron.placeOnHold(availableBook, command.getHoldDuration(), command.getTimestamp());
    return result.fold(this::publishEvents, this::publishEvents);
  }

  private Result publishEvents(BookPlacedOnHoldEvents placedOnHold) {
    patronRepository.publish(placedOnHold);
    return Success;
  }

  private Result publishEvents(BookHoldFailed bookHoldFailed) {
    patronRepository.publish(bookHoldFailed);
    return Result.Rejection;
  }

  private AvailableBook find(BookId id) {
    return findAvailableBook
        .findAvailableBookBy(id)
        .orElseThrow(() -> new BookNotFoundException(id));
  }

  private Patron find(PatronId patronId) {
    return patronRepository
        .findBy(patronId)
        .orElseThrow(() -> new PatronNotFoundException(patronId));
  }
}
