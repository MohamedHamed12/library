package io.pillopl.library.lending.patron.application.patron;

import io.pillopl.library.lending.patron.model.EmailAddressAlreadyRegistered;
import io.pillopl.library.lending.patron.model.PatronEvent.PatronCreated;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.Patrons;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static io.pillopl.library.lending.patron.model.PatronEvent.PatronCreated.createdAt;

@AllArgsConstructor
@Slf4j
public class RegisteringPatron {

    private final PatronIdGenerator patronIdGenerator;
    private final Patrons patronRepository;

    public Try<PatronId> register(@NonNull RegisterPatronCommand command) {
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