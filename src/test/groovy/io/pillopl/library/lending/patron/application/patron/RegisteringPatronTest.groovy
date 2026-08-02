package io.pillopl.library.lending.patron.application.patron

import io.pillopl.library.lending.patron.model.PatronEvent.PatronCreated
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.patron.model.Patrons
import io.vavr.control.Try
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId
import static io.pillopl.library.lending.patron.model.PatronFixture.regularPatron
import static io.pillopl.library.lending.patron.model.PatronType.Regular

class RegisteringPatronTest extends Specification {

    PatronId patronId = anyPatronId()
    PatronIdGenerator idGenerator = { patronId }
    Patrons repository = Mock()
    Instant now = Instant.parse("2999-01-01T00:00:00Z")

    def 'should successfully register regular patron and publish PatronCreated event'() {
        given:
            RegisteringPatron registeringPatron = new RegisteringPatron(idGenerator, repository)
            RegisterPatronCommand command = new RegisterPatronCommand(now, Regular)
        when:
            Try<PatronId> result = registeringPatron.register(command)
        then:
            result.isSuccess()
            result.get() == patronId
            1 * repository.publish({ PatronCreated created ->
                created.patronId == patronId.patronId && created.when == now && created.patronType == Regular
            }) >> regularPatron(patronId)
    }
}