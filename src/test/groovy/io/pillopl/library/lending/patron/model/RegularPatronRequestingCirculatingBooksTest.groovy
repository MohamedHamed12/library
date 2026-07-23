package io.pillopl.library.lending.patron.model

import io.pillopl.library.lending.book.model.AvailableBook
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.vavr.control.Either
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.book.model.BookFixture.*
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.HoldDuration.closeEnded
import static io.pillopl.library.lending.patron.model.PatronEvent.BookHoldFailed
import static io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHoldEvents
import static io.pillopl.library.lending.patron.model.PatronFixture.*
import static java.util.Collections.emptySet

class RegularPatronRequestingCirculatingBooksTest
        extends Specification {

    private static final Instant HOLD_TIME =
            Instant.parse('2026-07-21T10:15:30Z')

    def 'a regular patron cannot place on hold more than 5 books'() {
        when:
            Either<BookHoldFailed, BookPlacedOnHoldEvents> hold =
                    regularPatronWithHolds(holds)
                            .placeOnHold(
                                    circulatingBook(),
                                    closeEnded(HOLD_TIME, 3),
                                    HOLD_TIME
                            )

        then:
            hold.isLeft()

            BookHoldFailed event = hold.getLeft()

            event.reason.contains(
                    'patron cannot hold more books'
            )

        where:
            holds << [5, 6, 3000]
    }

    def 'a regular patron can place on hold book when he did not place on hold more than 4 books'() {
        given:
            AvailableBook book = circulatingBook()

        when:
            Either<BookHoldFailed, BookPlacedOnHoldEvents> hold =
                    regularPatronWithHolds(holds)
                            .placeOnHold(
                                    book,
                                    closeEnded(HOLD_TIME, 3),
                                    HOLD_TIME
                            )

        then:
            hold.isRight()

        where:
            holds << [0, 1, 2, 3, 4]
    }

    def 'a regular patron cannot place on hold books anymore when he has at least two overdue checkouts'() {
        given:
            LibraryBranchId libraryBranchId = anyBranch()

        when:
            Either<BookHoldFailed, BookPlacedOnHoldEvents> hold =
                    regularPatronWithOverdueCheckouts(
                            libraryBranchId,
                            books
                    ).placeOnHold(
                            circulatingAvailableBookAt(
                                    libraryBranchId
                            ),
                            closeEnded(HOLD_TIME, 3),
                            HOLD_TIME
                    )

        then:
            hold.isLeft()

            BookHoldFailed event = hold.getLeft()

            event.reason.contains(
                    'cannot place on hold when there are overdue checkouts'
            )

        where:
            books << [
                    [anyBookId(), anyBookId()] as Set,
                    [
                            anyBookId(),
                            anyBookId(),
                            anyBookId()
                    ] as Set
            ]
    }

    def 'a regular patron can place on hold books even though he has 2 overdue checkouts at different library'() {
        given:
            LibraryBranchId branch = anyBranch()
            LibraryBranchId differentBranch = anyBranch()

        when:
            Either<BookHoldFailed, BookPlacedOnHoldEvents> hold =
                    regularPatronWith3_OverdueCheckoutsAt(
                            branch
                    ).placeOnHold(
                            aBookAt(differentBranch),
                            closeEnded(HOLD_TIME, 3),
                            HOLD_TIME
                    )

        then:
            hold.isRight()
    }

    def 'a regular patron can place on hold books when he does not have 2 overdues'() {
        given:
            AvailableBook book = circulatingBook()

        when:
            Either<BookHoldFailed, BookPlacedOnHoldEvents> hold =
                    regularPatronWithOverdueCheckouts(
                            anyBranch(),
                            books
                    ).placeOnHold(
                            book,
                            closeEnded(HOLD_TIME, 3),
                            HOLD_TIME
                    )

        then:
            hold.isRight()

        where:
            books << [
                    [anyBookId()] as Set,
                    emptySet()
            ]
    }
}