package io.pillopl.library.lending.patron.application.patron;

import static io.pillopl.library.commons.commands.Result.Rejection;
import static io.pillopl.library.commons.commands.Result.Success;

import java.util.Objects;

import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.patron.application.hold.PatronNotFoundException;
import io.pillopl.library.lending.patron.model.Patron;
import io.pillopl.library.lending.patron.model.PatronEvent.PatronSuspended;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.Patrons;

public class SuspendingPatron {

  private final Patrons patrons;

  public SuspendingPatron(Patrons patrons) {
    this.patrons = patrons;
  }

  public Result suspend(SuspendPatronCommand command) {
    Objects.requireNonNull(command, "command");
    return findPatron(command.getPatronId())
        .suspend(command.getReason(), command.getTimestamp())
        .fold(failure -> Rejection, this::publish);
  }

  private Result publish(PatronSuspended suspended) {
    patrons.publish(suspended);
    return Success;
  }

  private Patron findPatron(PatronId patronId) {
    return patrons.findBy(patronId).orElseThrow(() -> new PatronNotFoundException(patronId));
  }
}
