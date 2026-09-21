package io.pillopl.library.lending.book;

import java.util.Optional;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.PatronReference;
import io.pillopl.library.lending.book.model.BookOnHold;

@FunctionalInterface
public interface FindBookOnHold {

  Optional<BookOnHold> findBookOnHold(BookId bookId, PatronReference patronId);
}
