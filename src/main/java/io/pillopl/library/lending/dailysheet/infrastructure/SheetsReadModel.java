package io.pillopl.library.lending.dailysheet.infrastructure;

import java.time.Instant;

import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import io.pillopl.library.lending.dailysheet.model.CheckoutsToOverdueSheet;
import io.pillopl.library.lending.dailysheet.model.DailySheet;
import io.pillopl.library.lending.dailysheet.model.HoldsToExpireSheet;
import io.pillopl.library.lending.patron.model.PatronEvent.BookCheckedOut;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldCanceled;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldExpired;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldExtended;
import io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHold;
import io.pillopl.library.lending.patron.model.PatronEvent.BookReturned;

class SheetsReadModel implements DailySheet {

  private final HoldSheetJpaRepository holds;
  private final CheckoutSheetJpaRepository checkouts;

  SheetsReadModel(HoldSheetJpaRepository holds, CheckoutSheetJpaRepository checkouts) {
    this.holds = holds;
    this.checkouts = checkouts;
  }

  @Override
  @Transactional(readOnly = true)
  public HoldsToExpireSheet queryForHoldsToExpireSheet(Instant processingTime) {
    return new HoldsToExpireSheet(
        holds.findByStatusAndHoldTillLessThanEqual(HoldSheetEntity.ACTIVE, processingTime).stream()
            .map(HoldSheetEntity::toExpiredHold)
            .toList());
  }

  @Override
  @Transactional(readOnly = true)
  public CheckoutsToOverdueSheet queryForCheckoutsToOverdue(Instant processingTime) {
    return new CheckoutsToOverdueSheet(
        checkouts
            .findByStatusAndCheckoutTillLessThanEqual(
                CheckoutSheetEntity.CHECKED_OUT, processingTime)
            .stream()
            .map(CheckoutSheetEntity::toOverdueCheckout)
            .toList());
  }

  @Override
  @Transactional
  @EventListener
  public void handle(BookPlacedOnHold event) {
    try {
      holds.saveAndFlush(HoldSheetEntity.from(event));
    } catch (DataIntegrityViolationException exception) {
      // Duplicate event: the unique event id makes this operation idempotent.
    }
  }

  @Override
  @Transactional
  @EventListener
  public void handle(BookHoldExtended event) {
    holds
        .findByStatusAndBookIdAndHoldByPatronIdAndHoldAtBranch(
            HoldSheetEntity.ACTIVE,
            event.getBookId(),
            event.getPatronId(),
            event.getLibraryBranchId())
        .forEach(hold -> hold.extendTo(event.getHoldTill()));
  }

  @Override
  @Transactional
  public void handle(BookHoldCanceled event) {
    holds
        .findByCanceledAtIsNullAndBookIdAndHoldByPatronId(event.getBookId(), event.getPatronId())
        .forEach(hold -> hold.cancelAt(event.getWhen()));
  }

  @Override
  @Transactional
  @EventListener
  public void handle(BookHoldExpired event) {
    holds
        .findByExpiredAtIsNullAndBookIdAndHoldByPatronId(event.getBookId(), event.getPatronId())
        .forEach(hold -> hold.expireAt(event.getWhen()));
  }

  @Override
  @Transactional
  @EventListener
  public void handle(BookCheckedOut event) {
    try {
      checkouts.saveAndFlush(CheckoutSheetEntity.from(event));
    } catch (DataIntegrityViolationException exception) {
      // Duplicate event: the unique event id makes this operation idempotent.
      return;
    }

    holds
        .findByCheckedOutAtIsNullAndBookIdAndHoldByPatronId(event.getBookId(), event.getPatronId())
        .forEach(hold -> hold.checkOutAt(event.getWhen()));
  }

  @Override
  @Transactional
  @EventListener
  public void handle(BookReturned event) {
    var activeCheckouts =
        checkouts.findByReturnedAtIsNullAndBookIdAndCheckedOutByPatronId(
            event.getBookId(), event.getPatronId());

    if (activeCheckouts.isEmpty()) {
      checkouts.save(CheckoutSheetEntity.returnedWithoutCheckout(event));
      return;
    }

    activeCheckouts.forEach(checkout -> checkout.returnAt(event.getWhen()));
  }
}
