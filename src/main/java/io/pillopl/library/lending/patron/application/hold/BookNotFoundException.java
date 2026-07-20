package io.pillopl.library.lending.patron.application.hold;

import io.pillopl.library.catalogue.BookId;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(BookId bookId) {
        super(
            "Cannot find available book with Id: "
                + bookId.getBookId()
        );
    }
}