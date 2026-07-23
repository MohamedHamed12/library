package io.pillopl.library.lending.patron.application.hold

import io.pillopl.library.lending.patron.model.HoldDuration
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId

class PlaceOnHoldCommandTest extends Specification {

    private static final Instant COMMAND_TIME =
            Instant.parse('2026-07-21T10:15:30Z')

    def 'should create closed-ended hold duration from command timestamp'() {
        given:
            PlaceOnHoldCommand command =
                    PlaceOnHoldCommand.closeEnded(
                            COMMAND_TIME,
                            anyPatronId(),
                            anyBranch(),
                            anyBookId(),
                            3
                    )

        when:
            HoldDuration duration = command.getHoldDuration()

        then:
            duration.from == COMMAND_TIME
            duration.to.get() ==
                    COMMAND_TIME.plus(Duration.ofDays(3))
    }

    def 'should create open-ended hold duration from command timestamp'() {
        given:
            PlaceOnHoldCommand command =
                    PlaceOnHoldCommand.openEnded(
                            COMMAND_TIME,
                            anyPatronId(),
                            anyBranch(),
                            anyBookId()
                    )

        when:
            HoldDuration duration = command.getHoldDuration()

        then:
            duration.from == COMMAND_TIME
            duration.to.isEmpty()
    }
}