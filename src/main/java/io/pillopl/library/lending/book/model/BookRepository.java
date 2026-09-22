package io.pillopl.library.lending.book.model;

import java.util.Optional;

import io.pillopl.library.catalogue.BookId;

public interface BookRepository {

  Optional<Book> findBy(BookId bookId);

  void save(Book book);
}
