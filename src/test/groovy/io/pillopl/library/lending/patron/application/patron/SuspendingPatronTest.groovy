package io.pillopl.library.lending.patron.application.patron

import io.pillopl.library.commons.commands.Result
import io.pillopl.library.lending.patron.application.hold.PatronNotFoundException
import io.pillopl.library.lending.patron.model.PatronEvent.PatronSuspended
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.patron.model.Patrons
import io.vavr.control.Option
import io.vavr.control.Try
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId
import static io.pillopl.library.lending.patron.model.PatronFixture.regularPatron
import static io.pillopl.library.lending.patron.model.PatronFixture.suspendedRegularPatron

class SuspendingPatronTest extends Specification {

    PatronId patronId = anyPatronId()
    Patrons repository = Mock()
    Instant now = Instant.parse("2999-01-01T00:00:00Z")

    def 'should suspend active patron and publish PatronSuspended event'() {
        given:
            SuspendingPatron suspendingPatron = new SuspendingPatron(repository)
            SuspendPatronCommand command = new SuspendPatronCommand(now, patronId, "Policy violation")
        and:
            repository.findBy(patronId) >> Option.of(regularPatron(patronId))
        when:
            Try<Result> result = suspendingPatron.suspend(command)
        then:
            result.isSuccess()
            result.get() == Result.Success
            1 * repository.publish({ PatronSuspended event ->
                event.patronId == patronId.patronId && event.when == now && event.reason == "Policy violation"
            }) >> regularPatron(patronId)
    }

    def 'should reject suspension when patron is already suspended'() {
        given:
            SuspendingPatron suspendingPatron = new SuspendingPatron(repository)
            SuspendPatronCommand command = new SuspendPatronCommand(now, patronId, "Another reason")
        and:
            repository.findBy(patronId) >> Option.of(suspendedRegularPatron())
        when:
            Try<Result> result = suspendingPatron.suspend(command)
        then:
            result.isSuccess()
            result.get() == Result.Rejection
            0 * repository.publish(_)
    }

    def 'should fail when patron does not exist'() {
        given:
            SuspendingPatron suspendingPatron = new SuspendingPatron(repository)
            SuspendPatronCommand command = new SuspendPatronCommand(now, patronId, "Policy violation")
        and:
            repository.findBy(patronId) >> Option.none()
        when:
            Try<Result> result = suspendingPatron.suspend(command)
        then:
            result.isFailure()
            result.getCause() instanceof PatronNotFoundException
            0 * repository.publish(_)
    }
}
