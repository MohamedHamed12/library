package io.pillopl.library.catalogue

import io.pillopl.library.commons.commands.Result
import io.pillopl.library.commons.events.DomainEvents
import io.vavr.control.Option
import io.vavr.control.Try
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

import static io.pillopl.library.catalogue.BookFixture.DDD_ISBN_STR
import static io.pillopl.library.catalogue.BookType.Restricted

class AddBookToCatalogueTest extends Specification {

    private static final Instant EVENT_TIME =
            Instant.parse('2026-07-21T16:00:00Z')

    CatalogueDatabase catalogueDatabase = Stub()
    DomainEvents domainEvents = Mock()
    Clock clock = Clock.fixed(EVENT_TIME, ZoneOffset.UTC)
    Catalogue catalogue = new Catalogue(catalogueDatabase, domainEvents, clock)

    def 'should add a new book to catalogue'() {
        given:
            databaseWorks()
        when:
            Try<Result> result = catalogue.addBook("Eric Evans", "DDD", DDD_ISBN_STR)
        then:
            result.isSuccess()
            result.get() == Result.Success

    }

    def 'should add a new book instance to catalogue'() {
        given:
            databaseWorks()
        and:
            thereIsBookWith(DDD_ISBN_STR)
        when:
            Try<Result> result = catalogue.addBookInstance(DDD_ISBN_STR, Restricted)
        then:
            result.isSuccess()
            result.get() == Result.Success
        and:
            1 * domainEvents.publish(_ as BookInstanceAddedToCatalogue)

    }

    def 'should reject adding a new book instance to catalogue when book isbn does not exist'() {
        given:
            databaseWorks()
        and:
            thereIsNoBookWith(DDD_ISBN_STR)
        when:
            Try<Result> result = catalogue.addBookInstance(DDD_ISBN_STR, Restricted)
        then:
            result.isSuccess()
            result.get() == Result.Rejection
        and:
            0 * domainEvents.publish(_ as BookInstanceAddedToCatalogue)

    }

    def 'should fail when adding a book if database fails'() {
        given:
            databaseDoesNotWork()
        when:
            Try<Result> result = catalogue.addBook("Eric Evans", "DDD", DDD_ISBN_STR)
        then:
            result.isFailure()
    }

    def 'should fail when adding a book instance if database fails'() {
        given:
            databaseDoesNotWork()
        and:
            thereIsBookWith(DDD_ISBN_STR)
        when:
            Try<Result> result = catalogue.addBookInstance(DDD_ISBN_STR, Restricted)
        then:
            result.isFailure()
        and:
            0 * domainEvents.publish(_ as BookInstanceAddedToCatalogue)
    }

    void databaseWorks() {
        catalogueDatabase.saveNew(_ as Book) >> null
        catalogueDatabase.saveNew(_ as BookInstance) >> null

    }

    void databaseDoesNotWork() {
        catalogueDatabase.saveNew(_ as Book) >> { (new IllegalStateException()) }
        catalogueDatabase.saveNew(_ as BookInstance) >> { (new IllegalStateException()) }

    }

    void thereIsBookWith(String isbn) {
        catalogueDatabase.findBy(new ISBN(isbn)) >> Option.of(BookFixture.DDD)
    }

    void thereIsNoBookWith(String isbn) {
        catalogueDatabase.findBy(new ISBN(isbn)) >> Option.none()
    }
}
