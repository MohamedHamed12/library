package io.pillopl.library.lending.book;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.book.model.AvailableBook;
import io.vavr.control.Option;

@FunctionalInterface
public interface FindAvailableBook {

  Option<AvailableBook> findAvailableBookBy(BookId bookId);
}
