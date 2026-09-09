package io.pillopl.library.lending.book.infrastructure

import io.pillopl.library.catalogue.BookId
import io.pillopl.library.lending.LendingTestContext
import io.pillopl.library.lending.book.model.AvailableBook
import io.pillopl.library.lending.book.model.BookOnHold
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronEvent
import io.pillopl.library.lending.patron.model.PatronId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.catalogue.BookType.Circulating
import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.book.model.BookFixture.circulatingAvailableBookAt
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.HoldDuration.closeEnded
import static io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHold.placedOnHoldAt
import static io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHoldEvents.events
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId

@SpringBootTest(classes = LendingTestContext.class)
class FindAvailableBookInDatabaseIT extends Specification {

    BookId bookId = anyBookId()
    LibraryBranchId libraryBranchId = anyBranch()
    PatronId patronId = anyPatronId()

    @Autowired
    BookDatabaseRepository bookEntityRepository

    def 'should find available book in database'() {
        given:
            AvailableBook availableBook = circulatingAvailableBookAt(bookId, libraryBranchId)
        when:
            bookEntityRepository.save(availableBook)
        then:
            bookEntityRepository.findAvailableBookBy(bookId).isDefined()
        when:
            BookOnHold bookOnHold = availableBook.handle(placedOnHold())
        and:
            bookEntityRepository.save(bookOnHold)
        then:
            bookEntityRepository.findAvailableBookBy(bookId).isEmpty()
    }


    PatronEvent.BookPlacedOnHold placedOnHold() {
        Instant now = Instant.now()
        return events(
                placedOnHoldAt(
                        now,
                        bookId, Circulating,
                        libraryBranchId, patronId,
                        closeEnded(now, 5)))
                .bookPlacedOnHold
    }

}
