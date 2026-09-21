package io.pillopl.library.lending.patron.application.patron;

import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.patron.application.hold.PatronNotFoundException;
import io.pillopl.library.lending.patron.model.Patron;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.Patrons;
import io.pillopl.library.lending.patron.model.PatronEvent.PatronSuspended;
import io.vavr.control.Try;

import static io.pillopl.library.commons.commands.Result.Rejection;
import static io.pillopl.library.commons.commands.Result.Success;

public class SuspendingPatron {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(SuspendingPatron.class);

    private final Patrons patrons;

    public SuspendingPatron(Patrons patrons) {
        this.patrons = patrons;
    }

    public Try<Result> suspend(SuspendPatronCommand command) {
        java.util.Objects.requireNonNull(command, "command");
        return Try.of(() -> findPatron(command.getPatronId())
                .suspend(command.getReason(), command.getTimestamp())
                .fold(
                        failure -> Rejection,
                        suspended -> publish(suspended)))
                .onFailure(t -> log.error("Failed to suspend patron", t));
    }

    private Result publish(PatronSuspended suspended) {
        patrons.publish(suspended);
        return Success;
    }

    private Patron findPatron(PatronId patronId) {
        return patrons
                .findBy(patronId)
                .getOrElseThrow(() -> new PatronNotFoundException(patronId));
    }
}
