package io.pillopl.library.lending.patron.model;

import static io.pillopl.library.lending.patron.model.PlacingOnHoldPolicy.allCurrentPolicies;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;

public class PatronFactory {

  public Patron create(
      PatronType patronType,
      PatronId patronId,
      EmailAddress emailAddress,
      Set<PatronHoldSnapshot> patronHolds,
      Map<LibraryBranchId, Set<BookId>> overdueCheckouts,
      PatronStatus status,
      String suspensionReason) {
    return new Patron(
        new PatronInformation(patronId, patronType, emailAddress),
        allCurrentPolicies(),
        new OverdueCheckouts(overdueCheckouts),
        new PatronHolds(
            patronHolds.stream()
                .map(
                    hold ->
                        new Hold(
                            hold.bookId(),
                            hold.libraryBranchId(),
                            hold.till(),
                            hold.extensionCount()))
                .collect(Collectors.toSet())),
        status,
        suspensionReason);
  }
}
