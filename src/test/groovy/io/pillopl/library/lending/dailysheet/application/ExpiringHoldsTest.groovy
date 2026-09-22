package io.pillopl.library.lending.dailysheet.application

import io.pillopl.library.commons.commands.BatchResult
import io.pillopl.library.lending.dailysheet.model.DailySheet
import io.pillopl.library.lending.dailysheet.model.ExpiredHold
import io.pillopl.library.lending.dailysheet.model.HoldsToExpireSheet
import io.pillopl.library.lending.patron.model.PatronEvent
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.patron.model.Patrons
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId

class ExpiringHoldsTest extends Specification {

    Patrons repository = Stub()
    DailySheet dailySheet = Stub()
    Clock clock = Clock.fixed(Instant.parse('2026-07-21T10:15:30Z'), ZoneOffset.UTC)

    PatronId patronWithExpiringHolds = anyPatronId()
    PatronId anotherPatronWithExpiringHolds = anyPatronId()

    ExpiringHolds expiring = new ExpiringHolds(dailySheet, repository, clock)

    def setup() {
        dailySheet.queryForHoldsToExpireSheet(_ as Instant) >>
                expiredHoldsBy(patronWithExpiringHolds, anotherPatronWithExpiringHolds)
    }

    def 'should return success if all holds were marked as expired'() {
        given:
            holdsWillBeExpiredSuccessfullyForBothPatrons()
        expect:
            expiring.expireHolds() == BatchResult.FullSuccess
    }

    def 'should return an error if at least one operation failed'() {
        given:
            expiringHoldWillFailForSecondPatron()
        expect:
            expiring.expireHolds() == BatchResult.SomeFailed
    }

    void expiringHoldWillFailForSecondPatron() {
        repository.publish(_ as PatronEvent) >>> [null, { throw new IllegalStateException() }]
    }

    void holdsWillBeExpiredSuccessfullyForBothPatrons() {
        repository.publish(_ as PatronEvent) >> null
    }

    HoldsToExpireSheet expiredHoldsBy(PatronId patronId, PatronId anotherPatronId) {
        return new HoldsToExpireSheet(List.of(
                new ExpiredHold(anyBookId(), patronId, anyBranch()),
                new ExpiredHold(anyBookId(), anotherPatronId, anyBranch())))
    }
}
