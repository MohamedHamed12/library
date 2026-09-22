package io.pillopl.library.lending.patron.application.patron;

import static io.pillopl.library.lending.patron.model.PatronEvent.PatronCreated.createdAt;

import java.util.Objects;

import io.pillopl.library.lending.patron.model.EmailAddressAlreadyRegistered;
import io.pillopl.library.lending.patron.model.PatronEvent.PatronCreated;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.Patrons;

public class RegisteringPatron {

  private final PatronIdGenerator patronIdGenerator;
  private final Patrons patronRepository;

  public RegisteringPatron(PatronIdGenerator patronIdGenerator, Patrons patronRepository) {
    this.patronIdGenerator = patronIdGenerator;
    this.patronRepository = patronRepository;
  }

  public PatronId register(RegisterPatronCommand command) {
    Objects.requireNonNull(command, "command");
    if (patronRepository.existsBy(command.getEmailAddress())) {
      throw new EmailAddressAlreadyRegistered(command.getEmailAddress());
    }

    PatronId patronId = patronIdGenerator.generate();
    PatronCreated patronCreated =
        createdAt(command.getTimestamp(), patronId, command.getType(), command.getEmailAddress());

    patronRepository.publish(patronCreated);
    return patronId;
  }
}
