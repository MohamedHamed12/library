package io.pillopl.library.lending.patron.application.patron

import io.pillopl.library.commons.commands.Result
import io.pillopl.library.lending.patron.application.hold.PatronNotFoundException
import io.pillopl.library.lending.patron.model.PatronEvent.PatronSuspended
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.patron.model.Patrons
import spock.lang.Specification

import java.time.Instant
import java.util.Optional

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
            repository.findBy(patronId) >> Optional.of(regularPatron(patronId))
        when:
            Result result = suspendingPatron.suspend(command)
        then:
            result == Result.Success
            1 * repository.publish({ PatronSuspended event ->
                event.getPatronId() == patronId.getPatronId() &&
                        event.getWhen() == now &&
                        event.getReason() == "Policy violation"
            }) >> regularPatron(patronId)
    }

    def 'should reject suspension when patron is already suspended'() {
        given:
            SuspendingPatron suspendingPatron = new SuspendingPatron(repository)
            SuspendPatronCommand command = new SuspendPatronCommand(now, patronId, "Another reason")
        and:
            repository.findBy(patronId) >> Optional.of(suspendedRegularPatron())
        when:
            Result result = suspendingPatron.suspend(command)
        then:
            result == Result.Rejection
            0 * repository.publish(_)
    }

    def 'should fail when patron does not exist'() {
        given:
            SuspendingPatron suspendingPatron = new SuspendingPatron(repository)
            SuspendPatronCommand command = new SuspendPatronCommand(now, patronId, "Policy violation")
        and:
            repository.findBy(patronId) >> Optional.empty()
        when:
            suspendingPatron.suspend(command)
        then:
            thrown(PatronNotFoundException)
            0 * repository.publish(_)
    }
}
