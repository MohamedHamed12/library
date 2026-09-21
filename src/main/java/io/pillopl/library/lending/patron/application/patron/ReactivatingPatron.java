package io.pillopl.library.lending.patron.application.patron;

import static io.pillopl.library.commons.commands.Result.Rejection;
import static io.pillopl.library.commons.commands.Result.Success;

import java.util.Objects;

import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.patron.application.hold.PatronNotFoundException;
import io.pillopl.library.lending.patron.model.Patron;
import io.pillopl.library.lending.patron.model.PatronEvent.PatronReactivated;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.Patrons;

public class ReactivatingPatron {

    private final Patrons patrons;

    public ReactivatingPatron(Patrons patrons) {
        this.patrons = patrons;
    }

    public Result reactivate(ReactivatePatronCommand command) {
        Objects.requireNonNull(command, "command");
        return findPatron(command.getPatronId())
                .reactivate(command.getTimestamp())
                .fold(failure -> Rejection, this::publish);
    }

    private Result publish(PatronReactivated reactivated) {
        patrons.publish(reactivated);
        return Success;
    }

    private Patron findPatron(PatronId patronId) {
        return patrons.findBy(patronId).orElseThrow(() -> new PatronNotFoundException(patronId));
    }
}
