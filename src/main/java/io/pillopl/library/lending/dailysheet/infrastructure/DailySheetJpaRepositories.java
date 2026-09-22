package io.pillopl.library.lending.dailysheet.infrastructure;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface HoldSheetJpaRepository extends JpaRepository<HoldSheetEntity, Long> {

  boolean existsByHoldEventId(UUID holdEventId);

  List<HoldSheetEntity> findByStatusAndHoldTillLessThanEqual(String status, Instant holdTill);

  List<HoldSheetEntity> findByStatusAndBookIdAndHoldByPatronIdAndHoldAtBranch(
      String status, UUID bookId, UUID holdByPatronId, UUID holdAtBranch);

  List<HoldSheetEntity> findByCanceledAtIsNullAndBookIdAndHoldByPatronId(
      UUID bookId, UUID holdByPatronId);

  List<HoldSheetEntity> findByExpiredAtIsNullAndBookIdAndHoldByPatronId(
      UUID bookId, UUID holdByPatronId);

  List<HoldSheetEntity> findByCheckedOutAtIsNullAndBookIdAndHoldByPatronId(
      UUID bookId, UUID holdByPatronId);
}

interface CheckoutSheetJpaRepository extends JpaRepository<CheckoutSheetEntity, Long> {

  boolean existsByCheckoutEventId(UUID checkoutEventId);

  List<CheckoutSheetEntity> findByStatusAndCheckoutTillLessThanEqual(
      String status, Instant checkoutTill);

  List<CheckoutSheetEntity> findByReturnedAtIsNullAndBookIdAndCheckedOutByPatronId(
      UUID bookId, UUID checkedOutByPatronId);
}
