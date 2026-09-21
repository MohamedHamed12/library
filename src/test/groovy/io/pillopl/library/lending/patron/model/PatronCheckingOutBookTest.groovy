package io.pillopl.library.lending.patron.model

import io.pillopl.library.lending.book.model.BookOnHold
import io.pillopl.library.commons.commands.Decision
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

import static io.pillopl.library.lending.patron.model.PatronEvent.BookCheckingOutFailed
import static PatronFixture.regularPatron
import static PatronFixture.regularPatronWith
import static io.pillopl.library.lending.book.model.BookFixture.bookOnHold
import static io.pillopl.library.lending.patron.model.CheckoutDuration.MAX_CHECKOUT_DURATION
import static io.pillopl.library.lending.patron.model.CheckoutDuration.maxDuration
import static io.pillopl.library.lending.patron.model.CheckoutDuration.forNoOfDays
import static io.pillopl.library.lending.patron.model.PatronEvent.BookCheckedOut
import static PatronFixture.onHold

class PatronCheckingOutBookTest extends Specification {

    private static final Instant CHECKOUT_TIME =
            Instant.parse('2026-07-21T12:00:00Z')

    def 'patron cannot check out book which is not placed on hold'() {
        when:
            Decision<BookCheckingOutFailed, BookCheckedOut> checkOut = regularPatron().checkOut(bookOnHold(), maxDuration(CHECKOUT_TIME), CHECKOUT_TIME)
        then:
		checkOut.rejection().isPresent()
            BookCheckingOutFailed e = checkOut.rejection().orElseThrow()
            e.reason.contains("book is not on hold by patron")
    }


    def 'patron can check out book which was placed on hold by him'() {
        given:
            Hold onHold = onHold()
        and:
            Patron patron = regularPatronWith(onHold)
        when:
            Decision<BookCheckingOutFailed, BookCheckedOut> checkOut = patron.checkOut(bookOnHold(onHold.bookId, onHold.libraryBranchId), maxDuration(CHECKOUT_TIME), CHECKOUT_TIME)
        then:
		checkOut.success().isPresent()
    }

    def 'patron can checkout up to 60 days'() {
        given:
            Hold onHold = onHold()
        and:
            Patron patron = regularPatronWith(onHold)
        and:
            BookOnHold bookOnHold = bookOnHold(onHold.bookId, onHold.libraryBranchId)
        when:
            Decision<BookCheckingOutFailed, BookCheckedOut> checkOut = patron.checkOut(bookOnHold, forNoOfDays(CHECKOUT_TIME, checkoutDays), CHECKOUT_TIME)
        then:
		checkOut.success().isPresent()
            verifyAll(checkOut.success().orElseThrow()) {
                assert it.libraryBranchId == bookOnHold.holdPlacedAt.libraryBranchId
                assert it.bookId == bookOnHold.bookInformation.bookId.bookId
                assert it.till == CHECKOUT_TIME.plus(Duration.ofDays(checkoutDays))
            }
        where:
            checkoutDays << (1 .. MAX_CHECKOUT_DURATION)
    }

    def 'patron cannot checkout for 0 or less'() {
        given:
            Hold onHold = onHold()
        and:
            Patron patron = regularPatronWith(onHold)
        when:
            patron.checkOut(bookOnHold(onHold.bookId, onHold.libraryBranchId), forNoOfDays(CHECKOUT_TIME, checkoutDays), CHECKOUT_TIME)
        then:
            thrown(IllegalArgumentException)
        where:
            checkoutDays << (-10 .. 0)
    }


}
