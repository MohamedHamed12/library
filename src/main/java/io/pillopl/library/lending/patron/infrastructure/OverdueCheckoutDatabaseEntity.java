package io.pillopl.library.lending.patron.infrastructure;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "overdue_checkout_database_entity")
class OverdueCheckoutDatabaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "patron_database_entity", nullable = false)
  private PatronDatabaseEntity patron;

  @Column(name = "patron_id", nullable = false)
  UUID patronId;

  @Column(name = "book_id", nullable = false)
  UUID bookId;

  @Column(name = "library_branch_id", nullable = false)
  UUID libraryBranchId;

  protected OverdueCheckoutDatabaseEntity() {}

  OverdueCheckoutDatabaseEntity(
      PatronDatabaseEntity patron, UUID bookId, UUID patronId, UUID libraryBranchId) {
    this.patron = patron;
    this.bookId = bookId;
    this.patronId = patronId;
    this.libraryBranchId = libraryBranchId;
  }

  UUID getLibraryBranchId() {
    return libraryBranchId;
  }

  boolean is(UUID patronId, UUID bookId, UUID libraryBranchId) {
    return this.patronId.equals(patronId)
        && this.bookId.equals(bookId)
        && this.libraryBranchId.equals(libraryBranchId);
  }
}
