package io.pillopl.library.lending.patron.model;

import static io.pillopl.library.commons.commands.Decision.accepted;
import static io.pillopl.library.commons.commands.Decision.rejected;

import java.util.List;

import io.pillopl.library.commons.commands.Decision;
import io.pillopl.library.lending.book.model.AvailableBook;

@FunctionalInterface
interface PlacingOnHoldPolicy {

  PlacingOnHoldPolicy onlyResearcherPatronsCanHoldRestrictedBooksPolicy =
      (AvailableBook toHold, Patron patron, HoldDuration holdDuration) -> {
        if (toHold.isRestricted() && patron.isRegular()) {
          return rejected(Rejection.withReason("Regular patrons cannot hold restricted books"));
        }
        return accepted(Allowance.ALLOWED);
      };

  PlacingOnHoldPolicy overdueCheckoutsRejectionPolicy =
      (AvailableBook toHold, Patron patron, HoldDuration holdDuration) -> {
        if (patron.overdueCheckoutsAt(toHold.getLibraryBranch())
            >= OverdueCheckouts.MAX_COUNT_OF_OVERDUE_RESOURCES) {
          return rejected(
              Rejection.withReason("cannot place on hold when there are overdue checkouts"));
        }
        return accepted(Allowance.ALLOWED);
      };

  PlacingOnHoldPolicy regularPatronMaximumNumberOfHoldsPolicy =
      (AvailableBook toHold, Patron patron, HoldDuration holdDuration) -> {
        if (patron.isRegular() && patron.numberOfHolds() >= PatronHolds.MAX_NUMBER_OF_HOLDS) {
          return rejected(Rejection.withReason("patron cannot hold more books"));
        }
        return accepted(Allowance.ALLOWED);
      };

  PlacingOnHoldPolicy onlyResearcherPatronsCanPlaceOpenEndedHolds =
      (AvailableBook toHold, Patron patron, HoldDuration holdDuration) -> {
        if (patron.isRegular() && holdDuration.isOpenEnded()) {
          return rejected(Rejection.withReason("regular patron cannot place open ended holds"));
        }
        return accepted(Allowance.ALLOWED);
      };

  Decision<Rejection, Allowance> evaluate(
      AvailableBook book, Patron patron, HoldDuration holdDuration);

  static List<PlacingOnHoldPolicy> allCurrentPolicies() {
    return List.of(
        onlyResearcherPatronsCanHoldRestrictedBooksPolicy,
        overdueCheckoutsRejectionPolicy,
        regularPatronMaximumNumberOfHoldsPolicy,
        onlyResearcherPatronsCanPlaceOpenEndedHolds);
  }
}

enum Allowance {
  ALLOWED
}
