package io.pillopl.library.catalogue;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "catalogue_book_instance")
class CatalogueBookInstanceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, length = 100)
  private String isbn;

  @Column(name = "book_id", nullable = false)
  private UUID bookId;

  protected CatalogueBookInstanceEntity() {}

  private CatalogueBookInstanceEntity(String isbn, UUID bookId) {
    this.isbn = isbn;
    this.bookId = bookId;
  }

  static CatalogueBookInstanceEntity from(BookInstance bookInstance) {
    return new CatalogueBookInstanceEntity(
        bookInstance.getBookIsbn().getIsbn(), bookInstance.getBookId().getBookId());
  }
}
