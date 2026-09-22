package io.pillopl.library.catalogue

import io.pillopl.library.commons.commands.Result
import io.pillopl.library.commons.events.DomainEvents
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Optional

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
        expect:
            catalogue.addBook("Eric Evans", "DDD", DDD_ISBN_STR) == Result.Success
    }

    def 'should add a new book instance to catalogue'() {
        given:
            databaseWorks()
        and:
            thereIsBookWith(DDD_ISBN_STR)
        when:
            Result result = catalogue.addBookInstance(DDD_ISBN_STR, Restricted)
        then:
            result == Result.Success
        and:
            1 * domainEvents.publish(_ as BookInstanceAddedToCatalogue)
    }

    def 'should reject adding a new book instance to catalogue when book isbn does not exist'() {
        given:
            databaseWorks()
        and:
            thereIsNoBookWith(DDD_ISBN_STR)
        when:
            Result result = catalogue.addBookInstance(DDD_ISBN_STR, Restricted)
        then:
            result == Result.Rejection
        and:
            0 * domainEvents.publish(_ as BookInstanceAddedToCatalogue)
    }

    def 'should propagate failure when adding a book if database fails'() {
        given:
            databaseDoesNotWork()
        when:
            catalogue.addBook("Eric Evans", "DDD", DDD_ISBN_STR)
        then:
            thrown(IllegalStateException)
    }

    def 'should propagate failure when adding a book instance if database fails'() {
        given:
            databaseDoesNotWork()
        and:
            thereIsBookWith(DDD_ISBN_STR)
        when:
            catalogue.addBookInstance(DDD_ISBN_STR, Restricted)
        then:
            thrown(IllegalStateException)
        and:
            0 * domainEvents.publish(_ as BookInstanceAddedToCatalogue)
    }

    void databaseWorks() {
        catalogueDatabase.saveNew(_ as Book) >> null
        catalogueDatabase.saveNew(_ as BookInstance) >> null
    }

    void databaseDoesNotWork() {
        catalogueDatabase.saveNew(_ as Book) >> { throw new IllegalStateException() }
        catalogueDatabase.saveNew(_ as BookInstance) >> { throw new IllegalStateException() }
    }

    void thereIsBookWith(String isbn) {
        catalogueDatabase.findBy(new ISBN(isbn)) >> Optional.of(BookFixture.DDD)
    }

    void thereIsNoBookWith(String isbn) {
        catalogueDatabase.findBy(new ISBN(isbn)) >> Optional.empty()
    }
}
