package io.pillopl.library.lending.book.application;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.book.model.Book;
import io.pillopl.library.lending.book.model.BookRepository;

class InMemoryBookRepo implements BookRepository {

  Map<BookId, Book> books = new HashMap<>();

  @Override
  public Optional<Book> findBy(BookId bookId) {
    return Optional.ofNullable(books.get(bookId));
  }

  @Override
  public void save(Book book) {
    books.put(book.bookId(), book);
  }
}
