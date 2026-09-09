package io.pillopl.library.lending.patron.application.hold;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.patron.model.Patron;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldExtended;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldExtensionFailed;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.Patrons;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import static io.pillopl.library.commons.commands.Result.Rejection;
import static io.pillopl.library.commons.commands.Result.Success;
import static io.vavr.API.*;
import static io.vavr.Patterns.$Left;
import static io.vavr.Patterns.$Right;

@AllArgsConstructor
public class ExtendingHold {

    private final FindBookOnHold findBookOnHold;
    private final Patrons patronRepository;

    public Try<Result> extendHold(@NonNull ExtendHoldCommand command) {
        return Try.of(() -> {
            BookOnHold bookOnHold = find(command.getBookId(), command.getPatronId());
            Patron patron = find(command.getPatronId());
            Either<BookHoldExtensionFailed, BookHoldExtended> result = patron.extendHold(
                    bookOnHold,
                    command.getAdditionalDays(),
                    command.getTimestamp());
            return Match(result).of(
                    Case($Left($()), this::publishEvents),
                    Case($Right($()), this::publishEvents));
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
                .getOrElseThrow(() -> new HoldNotFoundException(bookId));
    }

    private Patron find(PatronId patronId) {
        return patronRepository
                .findBy(patronId)
                .getOrElseThrow(() -> new PatronNotFoundException(patronId));
    }
}
