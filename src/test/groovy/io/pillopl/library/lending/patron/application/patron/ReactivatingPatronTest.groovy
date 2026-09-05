package io.pillopl.library.lending.patron.application.patron

import io.pillopl.library.commons.commands.Result
import io.pillopl.library.lending.patron.application.hold.PatronNotFoundException
import io.pillopl.library.lending.patron.model.PatronEvent.PatronReactivated
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.patron.model.Patrons
import io.vavr.control.Option
import io.vavr.control.Try
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId
import static io.pillopl.library.lending.patron.model.PatronFixture.regularPatron
import static io.pillopl.library.lending.patron.model.PatronFixture.suspendedRegularPatron

class ReactivatingPatronTest extends Specification {

    PatronId patronId = anyPatronId()
    Patrons repository = Mock()
    Instant now = Instant.parse("2999-01-01T00:00:00Z")

    def 'should reactivate suspended patron and publish PatronReactivated event'() {
        given:
            ReactivatingPatron reactivatingPatron = new ReactivatingPatron(repository)
            ReactivatePatronCommand command = new ReactivatePatronCommand(now, patronId)
        and:
            repository.findBy(patronId) >> Option.of(suspendedRegularPatron(patronId))
        when:
            Try<Result> result = reactivatingPatron.reactivate(command)
        then:
            result.isSuccess()
            result.get() == Result.Success
            1 * repository.publish({ PatronReactivated event ->
                event.patronId == patronId.patronId && event.when == now
            }) >> regularPatron(patronId)
    }

    def 'should reject reactivation when patron is already active'() {
        given:
            ReactivatingPatron reactivatingPatron = new ReactivatingPatron(repository)
            ReactivatePatronCommand command = new ReactivatePatronCommand(now, patronId)
        and:
            repository.findBy(patronId) >> Option.of(regularPatron(patronId))
        when:
            Try<Result> result = reactivatingPatron.reactivate(command)
        then:
            result.isSuccess()
            result.get() == Result.Rejection
            0 * repository.publish(_)
    }

    def 'should fail when patron does not exist'() {
        given:
            ReactivatingPatron reactivatingPatron = new ReactivatingPatron(repository)
            ReactivatePatronCommand command = new ReactivatePatronCommand(now, patronId)
        and:
            repository.findBy(patronId) >> Option.none()
        when:
            Try<Result> result = reactivatingPatron.reactivate(command)
        then:
            result.isFailure()
            result.getCause() instanceof PatronNotFoundException
            0 * repository.publish(_)
    }
}
