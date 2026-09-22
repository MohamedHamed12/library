package io.pillopl.library.catalogue;

import java.util.Optional;

class CatalogueDatabase {

  private final CatalogueBookJpaRepository bookRepository;
  private final CatalogueBookInstanceJpaRepository bookInstanceRepository;

  CatalogueDatabase(
      CatalogueBookJpaRepository bookRepository,
      CatalogueBookInstanceJpaRepository bookInstanceRepository) {
    this.bookRepository = bookRepository;
    this.bookInstanceRepository = bookInstanceRepository;
  }

  Book saveNew(Book book) {
    bookRepository.save(CatalogueBookEntity.from(book));
    return book;
  }

  BookInstance saveNew(BookInstance bookInstance) {
    bookInstanceRepository.save(CatalogueBookInstanceEntity.from(bookInstance));
    return bookInstance;
  }

  Optional<Book> findBy(ISBN isbn) {
    return bookRepository.findByIsbn(isbn.getIsbn()).map(CatalogueBookEntity::toDomainModel);
  }
}
