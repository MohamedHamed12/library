package io.pillopl.library.lending.book;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.PatronReference;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.vavr.control.Option;

@FunctionalInterface
public interface FindBookOnHold {

  Option<BookOnHold> findBookOnHold(BookId bookId, PatronReference patronId);
}
