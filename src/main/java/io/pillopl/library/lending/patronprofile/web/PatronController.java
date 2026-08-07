package io.pillopl.library.lending.patronprofile.web;

import io.micrometer.core.annotation.Timed;
import io.pillopl.library.lending.patron.application.patron.RegisterPatronCommand;
import io.pillopl.library.lending.patron.application.patron.RegisteringPatron;
import io.pillopl.library.lending.patron.model.EmailAddress;
import io.pillopl.library.lending.patron.model.PatronId;
import java.time.Clock;
import java.time.Instant;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@Timed(percentiles = { 0.5, 0.75, 0.95, 0.99 })
@RestController
@AllArgsConstructor
public class PatronController {

    private final RegisteringPatron registeringPatron;
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
}