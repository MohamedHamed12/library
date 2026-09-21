package io.pillopl.library.lending.patron.application.patron;

import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.patron.application.hold.PatronNotFoundException;
import io.pillopl.library.lending.patron.model.Patron;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.Patrons;
import io.pillopl.library.lending.patron.model.PatronEvent.PatronReactivated;
import io.vavr.control.Try;

import static io.pillopl.library.commons.commands.Result.Rejection;
import static io.pillopl.library.commons.commands.Result.Success;

public class ReactivatingPatron {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ReactivatingPatron.class);

    private final Patrons patrons;

    public ReactivatingPatron(Patrons patrons) {
        this.patrons = patrons;
    }

    public Try<Result> reactivate(ReactivatePatronCommand command) {
        java.util.Objects.requireNonNull(command, "command");
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
