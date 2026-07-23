package io.pillopl.library.lending.book.model

import io.pillopl.library.catalogue.BookId
import io.pillopl.library.catalogue.BookType
import io.pillopl.library.commons.aggregates.Version
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronEvent
import io.pillopl.library.lending.patron.model.PatronId

import java.time.Instant

import static io.pillopl.library.lending.book.model.BookFixture.version0
import static io.pillopl.library.catalogue.BookType.Circulating

class BookDSL {
    private static final Instant FIXED_TIME = Instant.parse('2026-07-21T14:00:00Z')

    BookType bookType
    BookId bookId
    LibraryBranchId libraryBranchId
    PatronId patronId
    Closure<Book> bookProvider
    Version version = version0()

    static BookDSL the(BookDSL book) {
        return book
    }

    static BookDSL aCirculatingBook() {
        return new BookDSL(Circulating)
    }

    BookDSL(BookType type) {
        this.bookType = type
    }

    BookDSL(BookDSL from) {
        this.bookType = from.bookType
        this.bookId = from.bookId
        this.libraryBranchId = from.libraryBranchId
        this.patronId = from.patronId
        this.bookProvider = from.bookProvider
        this.version = from.version
    }

    BookDSL with(BookId id) {
        this.bookId = id
        return this
    }

    BookDSL locatedIn(LibraryBranchId libraryBranch) {
        this.libraryBranchId = libraryBranch
        return this
    }

    BookDSL placedOnHoldBy(PatronId aPatron) {
        this.patronId = aPatron
        this.bookProvider = { ->
            new BookOnHold(new BookInformation(bookId, bookType), libraryBranchId, patronId, FIXED_TIME, version0())
        }
        return this
    }

    BookDSL stillAvailable() {
        bookProvider = { -> new AvailableBook(new BookInformation(bookId, bookType), libraryBranchId, version0()) }
        return this
    }

    BookDSL checkedOutBy(PatronId aPatron) {
        bookProvider = { ->
            new CheckedOutBook(new BookInformation(bookId, bookType), libraryBranchId, aPatron, version0())
        }
        return this
    }

    def isReturnedBy(PatronId aPatron) {
        return new BookDSL(this) {
            PatronEvent.BookReturned at(LibraryBranchId branchId) {
                return bookReturned(bookProvider(), aPatron, branchId)
            }
        }
    }

    def isPlacedOnHoldBy(PatronId aPatron) {
        return new BookDSL(this) {

            PatronId onHoldPatronId
            LibraryBranchId placeOnHoldBranchId
            Instant onHoldFrom

            {
                onHoldPatronId = aPatron
                onHoldFrom = FIXED_TIME
            }

            def at(LibraryBranchId branchId) {
                placeOnHoldBranchId = branchId
                return this
            }

            def from(Instant from) {
                onHoldFrom = from
                return this
            }

            PatronEvent.BookPlacedOnHold till(Instant till) {
                return bookPlacedOnHold(bookProvider(), onHoldPatronId, placeOnHoldBranchId, onHoldFrom, till)
            }
        }
    }

    def isCheckedOutBy(PatronId aPatron) {
        new BookDSL(this) {
            PatronEvent.BookCheckedOut at(LibraryBranchId branchId) {
                return bookCheckedOut(bookProvider(), aPatron, branchId)
            }
        }
    }

    PatronEvent.BookHoldCanceled isCancelledBy(PatronId aPatron) {
        return bookHoldCanceled(bookProvider(), aPatron, libraryBranchId)
    }

    PatronEvent.BookHoldExpired expired() {
        return bookHoldExpired(bookProvider(), patronId, libraryBranchId)
    }

    Book reactsTo(PatronEvent event) {
        return bookProvider().handle(event)
    }


    private static PatronEvent.BookReturned bookReturned(Book bookCheckedOut, PatronId patronId, LibraryBranchId libraryBranchId) {
        return new PatronEvent.BookReturned(FIXED_TIME,
                patronId.patronId,
                bookCheckedOut.getBookId().bookId,
                bookCheckedOut.bookInformation.bookType,
                libraryBranchId.libraryBranchId)
    }

    private static PatronEvent.BookCheckedOut bookCheckedOut(Book bookOnHold, PatronId patronId, LibraryBranchId libraryBranchId) {
        return new PatronEvent.BookCheckedOut(FIXED_TIME,
                patronId.patronId,
                bookOnHold.getBookId().bookId,
                bookOnHold.bookInformation.bookType,
                libraryBranchId.libraryBranchId,
                FIXED_TIME)
    }

    private static PatronEvent.BookPlacedOnHold bookPlacedOnHold(Book availableBook, PatronId byPatron, LibraryBranchId libraryBranchId, Instant from, Instant till) {
        return new PatronEvent.BookPlacedOnHold(FIXED_TIME,
                byPatron.patronId,
                availableBook.getBookId().bookId,
                availableBook.bookInformation.bookType,
                libraryBranchId.libraryBranchId,
                from,
                till)
    }


    private static PatronEvent.BookHoldExpired bookHoldExpired(Book bookOnHold, PatronId patronId, LibraryBranchId libraryBranchId) {
        return new PatronEvent.BookHoldExpired(FIXED_TIME,
                bookOnHold.getBookId().bookId,
                patronId.patronId,
                libraryBranchId.libraryBranchId)
    }

    private static PatronEvent.BookHoldCanceled bookHoldCanceled(Book bookOnHold, PatronId patronId, LibraryBranchId libraryBranchId) {
        return new PatronEvent.BookHoldCanceled(FIXED_TIME,
                bookOnHold.getBookId().bookId,
                patronId.patronId,
                libraryBranchId.libraryBranchId)
    }

}