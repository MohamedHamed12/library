package io.pillopl.library.lending.patron.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;

class HoldDatabaseEntity {

  @Id Long id;
  UUID patronId;
  UUID bookId;
  UUID libraryBranchId;
  Instant till;
  int extensionCount;

  HoldDatabaseEntity() {}

  HoldDatabaseEntity(UUID bookId, UUID patronId, UUID libraryBranchId, Instant till) {
    this.bookId = bookId;
    this.patronId = patronId;
    this.libraryBranchId = libraryBranchId;
    this.till = till;
    this.extensionCount = 0;
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

  Instant getTill() {
    return till;
  }

  int getExtensionCount() {
    return extensionCount;
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
