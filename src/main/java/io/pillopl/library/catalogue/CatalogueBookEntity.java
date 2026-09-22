package io.pillopl.library.catalogue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "catalogue_book")
class CatalogueBookEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, length = 100)
  private String isbn;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(nullable = false, length = 100)
  private String author;

  protected CatalogueBookEntity() {}

  private CatalogueBookEntity(String isbn, String title, String author) {
    this.isbn = isbn;
    this.title = title;
    this.author = author;
  }

  static CatalogueBookEntity from(Book book) {
    return new CatalogueBookEntity(
        book.getBookIsbn().getIsbn(), book.getTitle().getTitle(), book.getAuthor().getName());
  }

  Book toDomainModel() {
    return new Book(isbn, author, title);
  }
}
