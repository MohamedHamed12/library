package io.pillopl.library.lending.patron.model;

import static java.util.Collections.emptySet;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;

record OverdueCheckouts(Map<LibraryBranchId, Set<BookId>> overdueCheckouts) {

  static int MAX_COUNT_OF_OVERDUE_RESOURCES = 2;

  OverdueCheckouts {
    Objects.requireNonNull(overdueCheckouts, "overdueCheckouts");
  }

  Map<LibraryBranchId, Set<BookId>> getOverdueCheckouts() {
    return overdueCheckouts;
  }

  int countAt(LibraryBranchId libraryBranchId) {
    Objects.requireNonNull(libraryBranchId, "libraryBranchId");
    return overdueCheckouts.getOrDefault(libraryBranchId, emptySet()).size();
  }
}
