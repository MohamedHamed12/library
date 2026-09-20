package io.pillopl.library.lending.librarybranch.model;

import java.util.Objects;
import java.util.UUID;

public record LibraryBranchId(UUID libraryBranchId) {

  public LibraryBranchId {
    Objects.requireNonNull(libraryBranchId, "libraryBranchId");
  }

  public UUID getLibraryBranchId() {
    return libraryBranchId;
  }
}
