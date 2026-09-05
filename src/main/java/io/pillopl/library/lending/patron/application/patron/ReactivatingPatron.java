package io.pillopl.library.lending.patron.application.patron;

import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.patron.application.hold.PatronNotFoundException;
import io.pillopl.library.lending.patron.model.Patron;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.Patrons;
import io.pillopl.library.lending.patron.model.PatronEvent.PatronReactivated;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static io.pillopl.library.commons.commands.Result.Rejection;
import static io.pillopl.library.commons.commands.Result.Success;

@AllArgsConstructor
@Slf4j
public class ReactivatingPatron {

    private final Patrons patrons;

    public Try<Result> reactivate(@NonNull ReactivatePatronCommand command) {
        return Try.of(() -> findPatron(command.getPatronId())
                .reactivate(command.getTimestamp())
                .fold(
                        failure -> Rejection,
                        reactivated -> publish(reactivated)))
                .onFailure(t -> log.error("Failed to reactivate patron", t));
    }

    private Result publish(PatronReactivated reactivated) {
        patrons.publish(reactivated);
        return Success;
    }

    private Patron findPatron(PatronId patronId) {
        return patrons
                .findBy(patronId)
                .getOrElseThrow(() -> new PatronNotFoundException(patronId));
    }
}
