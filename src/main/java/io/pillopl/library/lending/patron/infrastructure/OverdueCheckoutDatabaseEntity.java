package io.pillopl.library.lending.patron.infrastructure;

import java.util.Objects;
import java.util.UUID;

import org.springframework.data.annotation.Id;

class OverdueCheckoutDatabaseEntity {

  @Id Long id;
  UUID patronId;
  UUID bookId;
  UUID libraryBranchId;

  OverdueCheckoutDatabaseEntity() {}

  OverdueCheckoutDatabaseEntity(UUID bookId, UUID patronId, UUID libraryBranchId) {
    this.bookId = bookId;
    this.patronId = patronId;
    this.libraryBranchId = libraryBranchId;
  }

  Long getId() {
    return id;
  }

  UUID getPatronId() {
    return patronId;
  }

  UUID getBookId() {
    return bookId;
  }

  UUID getLibraryBranchId() {
    return libraryBranchId;
  }

  boolean is(UUID patronId, UUID bookId, UUID libraryBranchId) {
    return this.patronId.equals(patronId)
        && this.bookId.equals(bookId)
        && this.libraryBranchId.equals(libraryBranchId);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof OverdueCheckoutDatabaseEntity that)) {
      return false;
    }
    return Objects.equals(id, that.id)
        && Objects.equals(patronId, that.patronId)
        && Objects.equals(bookId, that.bookId)
        && Objects.equals(libraryBranchId, that.libraryBranchId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, patronId, bookId, libraryBranchId);
  }
}
