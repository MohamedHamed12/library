package io.pillopl.library.lending.patron.application.hold;

import static io.pillopl.library.commons.commands.Result.Success;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.book.FindAvailableBook;
import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.lending.patron.model.*;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldFailed;
import io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHoldEvents;
import io.vavr.control.Either;
import io.vavr.control.Try;


public class PlacingOnHold {

  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(PlacingOnHold.class);

  private final FindAvailableBook findAvailableBook;
  private final Patrons patronRepository;

  public PlacingOnHold(FindAvailableBook findAvailableBook, Patrons patronRepository) {
    this.findAvailableBook = findAvailableBook;
    this.patronRepository = patronRepository;
  }

  public Try<Result> placeOnHold(PlaceOnHoldCommand command) {
    java.util.Objects.requireNonNull(command, "command");
    return Try.of(
            () -> {
              AvailableBook availableBook = find(command.getBookId());
              Patron patron = find(command.getPatronId());
              Either<BookHoldFailed, BookPlacedOnHoldEvents> result =
                  patron.placeOnHold(
                      availableBook, command.getHoldDuration(), command.getTimestamp());
              return result.fold(this::publishEvents, this::publishEvents);
            })
        .onFailure(t -> log.error("Failed to place a hold", t));
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
