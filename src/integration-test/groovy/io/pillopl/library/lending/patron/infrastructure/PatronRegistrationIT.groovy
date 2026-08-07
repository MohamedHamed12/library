package io.pillopl.library.lending.patron.infrastructure

import io.pillopl.library.lending.LendingTestContext
import io.pillopl.library.lending.patron.application.patron.RegisterPatronCommand
import io.pillopl.library.lending.patron.application.patron.RegisteringPatron
import io.pillopl.library.lending.patron.model.EmailAddress
import io.pillopl.library.lending.patron.model.EmailAddressAlreadyRegistered
import io.pillopl.library.lending.patron.model.Patron
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.patron.model.Patrons
import io.pillopl.library.lending.patronprofile.model.PatronProfiles
import io.pillopl.library.lending.patronprofile.model.PatronProfile
import io.vavr.control.Try
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.patron.model.PatronType.Regular
import static io.pillopl.library.lending.patron.model.PatronType.Researcher

@SpringBootTest(classes = LendingTestContext.class)
class PatronRegistrationIT extends Specification {

    static final Instant NOW = Instant.parse("2999-01-01T00:00:00Z")

    @Autowired
    RegisteringPatron registeringPatron

    @Autowired
    Patrons patrons

    @Autowired
    PatronProfiles patronProfiles

    def 'should register regular patron and load from repository and profile read model'() {
        when:
            Try<PatronId> result = registeringPatron.register(new RegisterPatronCommand(NOW, Regular, EmailAddress.of("regular@example.test")))
        then:
            result.isSuccess()
            PatronId patronId = result.get()
            patronId != null
        and:
            Patron loaded = patrons.findBy(patronId).get()
            loaded != null
        and:
            PatronProfile profile = patronProfiles.fetchFor(patronId)
            profile != null
    }

    def 'should register researcher patron and load from repository'() {
        when:
            Try<PatronId> result = registeringPatron.register(new RegisterPatronCommand(NOW, Researcher, EmailAddress.of("researcher@example.test")))
        then:
            result.isSuccess()
            PatronId patronId = result.get()
            patronId != null
        and:
            Patron loaded = patrons.findBy(patronId).get()
            loaded != null
    }

    def 'should reject registration with already registered email as case-insensitive duplicate'() {
        given:
            registeringPatron.register(new RegisterPatronCommand(NOW, Regular, EmailAddress.of("duplicate@example.test")))
        when:
            Try<PatronId> result = registeringPatron.register(new RegisterPatronCommand(NOW, Researcher, EmailAddress.of("Duplicate@Example.Test")))
        then:
            result.isFailure()
            result.getCause() instanceof EmailAddressAlreadyRegistered
    }

    def 'should round-trip normalized email through persistence'() {
        when:
            Try<PatronId> result = registeringPatron.register(new RegisterPatronCommand(NOW, Regular, EmailAddress.of("  Patron.White.Space@Example.test  ")))
        then:
            result.isSuccess()
            PatronId patronId = result.get()
            Patron loaded = patrons.findBy(patronId).get()
            loaded != null
        and:
            patrons.existsBy(EmailAddress.of("patron.white.space@example.test"))
    }
}