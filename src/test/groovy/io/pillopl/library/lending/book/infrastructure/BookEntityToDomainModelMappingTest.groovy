package io.pillopl.library.lending.book.infrastructure


import io.pillopl.library.lending.book.model.AvailableBook
import io.pillopl.library.lending.book.model.Book
import io.pillopl.library.catalogue.BookId
import io.pillopl.library.commons.aggregates.Version
import io.pillopl.library.lending.PatronReference
import io.pillopl.library.lending.book.model.BookOnHold
import io.pillopl.library.lending.book.model.CheckedOutBook
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.book.infrastructure.BookDatabaseEntity.BookState
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.catalogue.BookType.Circulating
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId
import static io.pillopl.library.lending.book.infrastructure.BookDatabaseEntity.BookState.*

class BookEntityToDomainModelMappingTest extends Specification {

    LibraryBranchId libraryBranchId = anyBranch()
    LibraryBranchId anotherBranchId = anyBranch()
    LibraryBranchId yetAnotherBranchId = anyBranch()
    PatronId patronId = anyPatronId()
    PatronId anotherPatronId = anyPatronId()
    BookId bookId = anyBookId()
    Instant holdTill = Instant.parse('2026-07-21T10:15:30Z')


    def 'should map to available book'() {
        given:
            BookDatabaseEntity entity = bookEntity(Available)
        when:
            Book book = entity.toDomainModel()
        and:
            AvailableBook availableBook = book as AvailableBook
        then:
            availableBook.bookId == bookId
            availableBook.bookInformation.bookType == Circulating
            availableBook.libraryBranch == libraryBranchId

    }

    def 'should map to on hold book'() {
        given:
            BookDatabaseEntity entity = bookEntity(OnHold)
        when:
            Book book = entity.toDomainModel()
        and:
            BookOnHold bookOnHold = book as BookOnHold
        then:
            bookOnHold.bookId == bookId
            bookOnHold.bookInformation.bookType == Circulating
            bookOnHold.holdPlacedAt == anotherBranchId
            bookOnHold.byPatron == patronId
            bookOnHold.holdTill == holdTill
    }

    def 'should map to checked out book'() {
        given:
            BookDatabaseEntity entity = bookEntity(CheckedOut)
        when:
            Book book = entity.toDomainModel()
        and:
            CheckedOutBook checkedOutBook = book as CheckedOutBook
        then:
            checkedOutBook.bookId == bookId
            checkedOutBook.bookInformation.bookType == Circulating
            checkedOutBook.checkedOutAt == yetAnotherBranchId
            checkedOutBook.byPatron == anotherPatronId
    }

    BookDatabaseEntity bookEntity(BookState state) {
        switch (state) {
            case Available:
                return BookDatabaseEntity.from(
                        new AvailableBook(bookId, Circulating, libraryBranchId, new Version(0)))
            case OnHold:
                return BookDatabaseEntity.from(
                        new BookOnHold(
                                bookId,
                                Circulating,
                                anotherBranchId,
                                PatronReference.of(patronId.patronId),
                                holdTill,
                                new Version(0)))
            case CheckedOut:
                return BookDatabaseEntity.from(
                        new CheckedOutBook(
                                bookId,
                                Circulating,
                                yetAnotherBranchId,
                                PatronReference.of(anotherPatronId.patronId),
                                new Version(0)))
        }
    }
}
