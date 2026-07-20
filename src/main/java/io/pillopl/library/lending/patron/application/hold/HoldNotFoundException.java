package io.pillopl.library.lending.patron.application.hold;

import io.pillopl.library.catalogue.BookId;

public class HoldNotFoundException extends RuntimeException {

    public HoldNotFoundException(BookId bookId) {
        super(
            "Cannot find book on hold with Id: "
                + bookId.getBookId()
        );
    }
}