package io.pillopl.library.lending.patron.application.patron;

import io.pillopl.library.lending.patron.model.EmailAddressAlreadyRegistered;
import io.pillopl.library.lending.patron.model.PatronEvent.PatronCreated;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.Patrons;
import io.vavr.control.Try;

import static io.pillopl.library.lending.patron.model.PatronEvent.PatronCreated.createdAt;

public class RegisteringPatron {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(RegisteringPatron.class);

    private final PatronIdGenerator patronIdGenerator;
    private final Patrons patronRepository;

    public RegisteringPatron(PatronIdGenerator patronIdGenerator, Patrons patronRepository) {
        this.patronIdGenerator = patronIdGenerator;
        this.patronRepository = patronRepository;
    }

    public Try<PatronId> register(RegisterPatronCommand command) {
        java.util.Objects.requireNonNull(command, "command");
        return Try.of(() -> {
            if (patronRepository.existsBy(command.getEmailAddress())) {
                throw new EmailAddressAlreadyRegistered(command.getEmailAddress());
            }

            PatronId patronId = patronIdGenerator.generate();
            PatronCreated patronCreated = createdAt(
                    command.getTimestamp(),
                    patronId,
                    command.getType(),
                    command.getEmailAddress());

            patronRepository.publish(patronCreated);
            return patronId;
        }).onFailure(t -> log.error("Failed to register patron", t));
    }
}