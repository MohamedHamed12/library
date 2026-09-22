package io.pillopl.library.lending.patronprofile.infrastructure;

import org.springframework.transaction.annotation.Transactional;

import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.PatronStatus;
import io.pillopl.library.lending.patronprofile.model.CheckoutsView;
import io.pillopl.library.lending.patronprofile.model.HoldsView;
import io.pillopl.library.lending.patronprofile.model.PatronProfile;
import io.pillopl.library.lending.patronprofile.model.PatronProfiles;

class PatronProfileReadModel implements PatronProfiles {

  private final PatronStatusViewJpaRepository patrons;
  private final PatronHoldViewJpaRepository holds;
  private final PatronCheckoutViewJpaRepository checkouts;

  PatronProfileReadModel(
      PatronStatusViewJpaRepository patrons,
      PatronHoldViewJpaRepository holds,
      PatronCheckoutViewJpaRepository checkouts) {
    this.patrons = patrons;
    this.holds = holds;
    this.checkouts = checkouts;
  }

  @Override
  @Transactional(readOnly = true)
  public PatronProfile fetchFor(PatronId patronId) {
    var patronIdValue = patronId.getPatronId();

    PatronStatus status =
        patrons
            .findByPatronId(patronIdValue)
            .map(PatronStatusViewEntity::toDomainStatus)
            .orElseThrow(() -> new IllegalStateException("Patron not found: " + patronIdValue));

    HoldsView holdsView =
        new HoldsView(
            holds
                .findByHoldByPatronIdAndCheckedOutAtIsNullAndExpiredAtIsNullAndCanceledAtIsNull(
                    patronIdValue)
                .stream()
                .map(PatronHoldViewEntity::toDomainHold)
                .toList());

    CheckoutsView checkoutsView =
        new CheckoutsView(
            checkouts.findByCheckedOutByPatronIdAndReturnedAtIsNull(patronIdValue).stream()
                .map(PatronCheckoutViewEntity::toDomainCheckout)
                .toList());

    return new PatronProfile(status, holdsView, checkoutsView);
  }
}
