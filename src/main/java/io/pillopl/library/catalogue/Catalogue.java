package io.pillopl.library.catalogue;

import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.commons.events.DomainEvents;
import io.vavr.control.Try;
import java.time.Clock;
import java.time.Instant;

import static io.pillopl.library.commons.commands.Result.Rejection;
import static io.pillopl.library.commons.commands.Result.Success;

public class Catalogue {

    private final CatalogueDatabase database;
    private final DomainEvents domainEvents;
    private final Clock clock;

    Catalogue(CatalogueDatabase database, DomainEvents domainEvents, Clock clock) {
        this.database = database;
        this.domainEvents = domainEvents;
        this.clock = clock;
    }

    public Try<Result> addBook(String author, String title, String isbn) {
        return Try.of(() -> {
            Book book = new Book(isbn, author, title);
            database.saveNew(book);
            return Success;
        });
    }

    public Try<Result> addBookInstance(String isbn, BookType bookType) {
        return Try.of(() -> {
            Instant timestamp = clock.instant();
            return database
                .findBy(new ISBN(isbn))
                .map(book -> BookInstance.instanceOf(book, bookType))
                .map(bookInstance -> saveAndPublishEvent(bookInstance, timestamp))
                .map(savedInstance -> Success)
                .orElse(Rejection);
        });
    }

    private BookInstance saveAndPublishEvent(BookInstance bookInstance, Instant timestamp) {
        database.saveNew(bookInstance);
        domainEvents.publish(BookInstanceAddedToCatalogue.addedAt(timestamp, bookInstance));
        return bookInstance;
    }


}

