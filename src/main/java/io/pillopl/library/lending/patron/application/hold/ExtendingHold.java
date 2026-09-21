package io.pillopl.library.lending.patron.application.hold;

import static io.pillopl.library.commons.commands.Result.Rejection;
import static io.pillopl.library.commons.commands.Result.Success;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.book.FindBookOnHold;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.patron.model.Patron;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldExtended;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldExtensionFailed;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.Patrons;
import io.vavr.control.Either;
import io.vavr.control.Try;


public class ExtendingHold {

  private final FindBookOnHold findBookOnHold;
  private final Patrons patronRepository;

  public ExtendingHold(FindBookOnHold findBookOnHold, Patrons patronRepository) {
    this.findBookOnHold = findBookOnHold;
    this.patronRepository = patronRepository;
  }

  public Try<Result> extendHold(ExtendHoldCommand command) {
    java.util.Objects.requireNonNull(command, "command");
    return Try.of(
        () -> {
          BookOnHold bookOnHold = find(command.getBookId(), command.getPatronId());
          Patron patron = find(command.getPatronId());
          Either<BookHoldExtensionFailed, BookHoldExtended> result =
              patron.extendHold(bookOnHold, command.getAdditionalDays(), command.getTimestamp());
          return result.fold(this::publishEvents, this::publishEvents);
        });
  }

  private Result publishEvents(BookHoldExtended bookHoldExtended) {
    patronRepository.publish(bookHoldExtended);
    return Success;
  }

  private Result publishEvents(BookHoldExtensionFailed bookHoldExtensionFailed) {
    patronRepository.publish(bookHoldExtensionFailed);
    return Rejection;
  }

  private BookOnHold find(BookId bookId, PatronId patronId) {
    return findBookOnHold
        .findBookOnHold(bookId, patronId)
        .orElseThrow(() -> new HoldNotFoundException(bookId));
  }

  private Patron find(PatronId patronId) {
    return patronRepository
        .findBy(patronId)
        .orElseThrow(() -> new PatronNotFoundException(patronId));
  }
}
