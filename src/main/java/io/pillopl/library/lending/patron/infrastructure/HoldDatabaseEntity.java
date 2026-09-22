package io.pillopl.library.lending.patron.infrastructure;

import java.time.Instant;
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
@Table(name = "hold_database_entity")
class HoldDatabaseEntity {

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

  @Column(name = "till")
  Instant till;

  @Column(name = "extension_count", nullable = false)
  int extensionCount;

  protected HoldDatabaseEntity() {}

  HoldDatabaseEntity(
      PatronDatabaseEntity patron, UUID bookId, UUID patronId, UUID libraryBranchId, Instant till) {
    this.patron = patron;
    this.bookId = bookId;
    this.patronId = patronId;
    this.libraryBranchId = libraryBranchId;
    this.till = till;
    this.extensionCount = 0;
  }

  UUID getLibraryBranchId() {
    return libraryBranchId;
  }

  void extendTo(Instant till, int extensionCount) {
    this.till = till;
    this.extensionCount = extensionCount;
  }

  boolean is(UUID patronId, UUID bookId, UUID libraryBranchId) {
    return this.patronId.equals(patronId)
        && this.bookId.equals(bookId)
        && this.libraryBranchId.equals(libraryBranchId);
  }
}
