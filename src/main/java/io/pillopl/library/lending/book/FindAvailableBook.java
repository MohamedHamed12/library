package io.pillopl.library.lending.book;

import java.util.Optional;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.book.model.AvailableBook;

@FunctionalInterface
public interface FindAvailableBook {

  Optional<AvailableBook> findAvailableBookBy(BookId bookId);
}
