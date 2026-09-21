package io.pillopl.library.lending.patron.application.patron

import io.pillopl.library.commons.commands.Result
import io.pillopl.library.lending.patron.application.hold.PatronNotFoundException
import io.pillopl.library.lending.patron.model.PatronEvent.PatronReactivated
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.patron.model.Patrons
import spock.lang.Specification

import java.time.Instant
import java.util.Optional

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
            repository.findBy(patronId) >> Optional.of(suspendedRegularPatron(patronId))
        when:
            Result result = reactivatingPatron.reactivate(command)
        then:
            result == Result.Success
            1 * repository.publish({ PatronReactivated event ->
                event.getPatronId() == patronId.getPatronId() && event.getWhen() == now
            }) >> regularPatron(patronId)
    }

    def 'should reject reactivation when patron is already active'() {
        given:
            ReactivatingPatron reactivatingPatron = new ReactivatingPatron(repository)
            ReactivatePatronCommand command = new ReactivatePatronCommand(now, patronId)
        and:
            repository.findBy(patronId) >> Optional.of(regularPatron(patronId))
        when:
            Result result = reactivatingPatron.reactivate(command)
        then:
            result == Result.Rejection
            0 * repository.publish(_)
    }

    def 'should fail when patron does not exist'() {
        given:
            ReactivatingPatron reactivatingPatron = new ReactivatingPatron(repository)
            ReactivatePatronCommand command = new ReactivatePatronCommand(now, patronId)
        and:
            repository.findBy(patronId) >> Optional.empty()
        when:
            reactivatingPatron.reactivate(command)
        then:
            thrown(PatronNotFoundException)
            0 * repository.publish(_)
    }
}
