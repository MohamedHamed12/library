package io.pillopl.library.lending.patronprofile.web;

import io.micrometer.core.annotation.Timed;
import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.patron.application.patron.ReactivatePatronCommand;
import io.pillopl.library.lending.patron.application.patron.ReactivatingPatron;
import io.pillopl.library.lending.patron.application.patron.RegisterPatronCommand;
import io.pillopl.library.lending.patron.application.patron.RegisteringPatron;
import io.pillopl.library.lending.patron.application.patron.SuspendPatronCommand;
import io.pillopl.library.lending.patron.application.patron.SuspendingPatron;
import io.pillopl.library.lending.patron.model.EmailAddress;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patronprofile.web.error.ApiErrorCode;
import io.pillopl.library.lending.patronprofile.web.error.ApiException;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@Timed(percentiles = { 0.5, 0.75, 0.95, 0.99 })
@RestController
@AllArgsConstructor
public class PatronController {

    private final RegisteringPatron registeringPatron;
    private final SuspendingPatron suspendingPatron;
    private final ReactivatingPatron reactivatingPatron;
    private final Clock clock;

    @PostMapping("/patrons")
    public ResponseEntity<Void> registerPatron(
            @Valid @RequestBody RegisterPatronRequest request,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        PatronId patronId = registeringPatron.register(
                new RegisterPatronCommand(Instant.now(clock), request.getType(), EmailAddress.of(request.getEmail()))
        ).get();

        return ResponseEntity
                .created(uriComponentsBuilder.path("/profiles/{patronId}").buildAndExpand(patronId.getPatronId()).toUri())
                .build();
    }

    @PostMapping("/patrons/{patronId}/suspension")
    public ResponseEntity<Void> suspend(
            @PathVariable UUID patronId,
            @Valid @RequestBody SuspendPatronRequest request
    ) {
        SuspendPatronCommand command = new SuspendPatronCommand(
                Instant.now(clock),
                new PatronId(patronId),
                request.getReason());

        Result result = suspendingPatron.suspend(command).get();

        rejectIfNeeded(
                result,
                ApiErrorCode.PATRON_ALREADY_SUSPENDED,
                "The patron is already suspended.");

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/patrons/{patronId}/suspension")
    public ResponseEntity<Void> reactivate(@PathVariable UUID patronId) {
        ReactivatePatronCommand command = new ReactivatePatronCommand(
                Instant.now(clock),
                new PatronId(patronId));

        Result result = reactivatingPatron.reactivate(command).get();

        rejectIfNeeded(
                result,
                ApiErrorCode.PATRON_ALREADY_ACTIVE,
                "The patron is already active.");

        return ResponseEntity.noContent().build();
    }

    private void rejectIfNeeded(Result result, ApiErrorCode code, String message) {
        if (result == Result.Rejection) {
            throw ApiException.conflict(code, message);
        }
    }
}
