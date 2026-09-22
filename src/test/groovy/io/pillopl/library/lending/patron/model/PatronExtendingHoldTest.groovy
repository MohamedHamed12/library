package io.pillopl.library.lending.patron.model

import io.pillopl.library.catalogue.BookId
import io.pillopl.library.commons.aggregates.Version
import io.pillopl.library.lending.book.model.BookOnHold
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import spock.lang.Specification

import java.time.Instant
import java.util.Collections
import java.util.HashMap

import static io.pillopl.library.catalogue.BookType.Circulating
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronType.Regular
import static io.pillopl.library.lending.patron.model.PatronType.Researcher
import static io.pillopl.library.lending.patron.model.PlacingOnHoldPolicy.allCurrentPolicies

class PatronExtendingHoldTest extends Specification {

    private static final Instant NOW = Instant.parse('2026-09-09T10:00:00Z')
    private static final Instant TILL = Instant.parse('2026-09-12T10:00:00Z')

    PatronId patronId = PatronFixture.anyPatronId()
    BookId bookId = io.pillopl.library.lending.book.model.BookFixture.anyBookId()
    LibraryBranchId branchId = anyBranch()

    def 'regular patron can extend a current closed-ended hold once by at most seven days'() {
        given:
            Patron patron = patron(Regular, TILL, 0)
            BookOnHold book = bookOnHold(TILL)
        when:
            def result = patron.extendHold(book, NumberOfDays.of(7), NOW)
        then:
            result.success().isPresent()
            result.success().orElseThrow().previousHoldTill == TILL
            result.success().orElseThrow().holdTill == Instant.parse('2026-09-19T10:00:00Z')
            result.success().orElseThrow().extensionCount == 1
    }

    def 'regular patron cannot extend a hold twice'() {
        given:
            Patron patron = patron(Regular, TILL, 1)
        expect:
            patron.extendHold(bookOnHold(TILL), NumberOfDays.of(1), NOW).rejection().isPresent()
    }

    def 'regular patron cannot extend by more than seven days'() {
        expect:
            patron(Regular, TILL, 0)
                    .extendHold(bookOnHold(TILL), NumberOfDays.of(8), NOW)
                    .rejection().isPresent()
    }

    def 'researcher can extend a hold twice by at most fourteen days each'() {
        given:
            Patron firstExtension = patron(Researcher, TILL, 0)
            Patron secondExtension = patron(Researcher, TILL, 1)
        expect:
            firstExtension.extendHold(bookOnHold(TILL), NumberOfDays.of(14), NOW).success().isPresent()
            secondExtension.extendHold(bookOnHold(TILL), NumberOfDays.of(14), NOW).success().isPresent()
    }

    def 'researcher cannot extend a hold a third time'() {
        expect:
            patron(Researcher, TILL, 2)
                    .extendHold(bookOnHold(TILL), NumberOfDays.of(1), NOW)
                    .rejection().isPresent()
    }

    def 'open-ended hold cannot be extended'() {
        expect:
            patron(Regular, null, 0)
                    .extendHold(bookOnHold(null), NumberOfDays.of(1), NOW)
                    .rejection().isPresent()
    }

    def 'hold is expired at its exact expiration timestamp'() {
        expect:
            patron(Regular, TILL, 0)
                    .extendHold(bookOnHold(TILL), NumberOfDays.of(1), TILL)
                    .rejection().isPresent()
    }

    def 'extension is calculated from current expiration rather than request time'() {
        when:
            def result = patron(Regular, TILL, 0)
                    .extendHold(bookOnHold(TILL), NumberOfDays.of(2), NOW)
        then:
            result.success().isPresent()
            result.success().orElseThrow().holdTill == Instant.parse('2026-09-14T10:00:00Z')
    }

    def 'patron cannot extend a hold owned by another patron'() {
        given:
            Patron patronWithoutBook = new Patron(
                    PatronFixture.patronInformation(patronId, Regular),
                    allCurrentPolicies(),
                    new OverdueCheckouts(new HashMap<>()),
                    new PatronHolds(Collections.emptySet()),
                    PatronStatus.ACTIVE,
                    null)
        expect:
            patronWithoutBook.extendHold(bookOnHold(TILL), NumberOfDays.of(1), NOW).rejection().isPresent()
    }

    private Patron patron(PatronType type, Instant till, int extensionCount) {
        return new Patron(
                PatronFixture.patronInformation(patronId, type),
                allCurrentPolicies(),
                new OverdueCheckouts(new HashMap<>()),
                new PatronHolds(Collections.singleton(new Hold(bookId, branchId, till, extensionCount))),
                PatronStatus.ACTIVE,
                null)
    }

    private BookOnHold bookOnHold(Instant till) {
        return new BookOnHold(bookId, Circulating, branchId, patronId, till, new Version(0))
    }
}
