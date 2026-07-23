package io.pillopl.library.lending.patron.model

import io.pillopl.library.lending.book.model.BookFixture
import io.pillopl.library.lending.book.model.BookOnHold
import io.vavr.control.Either
import spock.lang.Specification

import java.time.Instant

import static PatronFixture.regularPatron
import static PatronFixture.regularPatronWithHold
import static PatronEvent.BookHoldCanceled
import static PatronEvent.BookHoldCancelingFailed


class PatronCancelingHoldTest extends Specification {

    private static final Instant CANCEL_TIME = Instant.parse('2026-07-21T10:15:30Z')

    def 'patron should be able to cancel his holds'() {
        given:
            BookOnHold forBook = BookFixture.bookOnHold()
        and:
            Patron patron = regularPatronWithHold(forBook)
        when:
            Either<BookHoldCancelingFailed, BookHoldCanceled> cancelHold = patron.cancelHold(forBook, CANCEL_TIME)
        then:
            cancelHold.isRight()
            cancelHold.get().with {
                assert it.libraryBranchId == forBook.getHoldPlacedAt().libraryBranchId
                assert it.bookId == forBook.bookInformation.bookId.bookId
            }

    }

    def 'patron cannot cancel a hold which does not exist'() {
        given:
            BookOnHold forBook = BookFixture.bookOnHold()
        and:
            Patron patron = regularPatron()
        when:
            Either<BookHoldCancelingFailed, BookHoldCanceled> cancelHold = patron.cancelHold(forBook, CANCEL_TIME)
        then:
            cancelHold.isLeft()

    }

    def 'patron cannot cancel a hold which was done by someone else'() {
        given:
            BookOnHold forBook = BookFixture.bookOnHold()
        and:
            Patron patron = regularPatron()
        and:
            Patron differentPatron = regularPatronWithHold(forBook)
        when:
            Either<BookHoldCancelingFailed, BookHoldCanceled> cancelHold = patron.cancelHold(forBook, CANCEL_TIME)
        then:
            cancelHold.isLeft()

    }

}
