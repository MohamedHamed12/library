package io.pillopl.library.lending.dailysheet.infrastructure;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.dailysheet.model.ExpiredHold;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHold;
import io.pillopl.library.lending.patron.model.PatronId;

@Entity
@Table(name = "holds_sheet")
class HoldSheetEntity {

  static final String ACTIVE = "ACTIVE";
  static final String CANCELED = "CANCELED";
  static final String EXPIRED = "EXPIRED";
  static final String CHECKED_OUT = "CHECKEDOUT";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "book_id", nullable = false)
  private UUID bookId;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "hold_event_id", unique = true)
  private UUID holdEventId;

  @Column(name = "hold_at_branch")
  private UUID holdAtBranch;

  @Column(name = "hold_by_patron_id")
  private UUID holdByPatronId;

  @Column(name = "hold_at")
  private Instant holdAt;

  @Column(name = "hold_till")
  private Instant holdTill;

  @Column(name = "expired_at")
  private Instant expiredAt;

  @Column(name = "canceled_at")
  private Instant canceledAt;

  @Column(name = "checked_out_at")
  private Instant checkedOutAt;

  protected HoldSheetEntity() {}

  private HoldSheetEntity(BookPlacedOnHold event) {
    bookId = event.getBookId();
    status = ACTIVE;
    holdEventId = event.getEventId();
    holdAtBranch = event.getLibraryBranchId();
    holdByPatronId = event.getPatronId();
    holdAt = event.getWhen();
    holdTill = event.getHoldTill();
  }

  static HoldSheetEntity from(BookPlacedOnHold event) {
    return new HoldSheetEntity(event);
  }

  ExpiredHold toExpiredHold() {
    return new ExpiredHold(
        new BookId(bookId), new PatronId(holdByPatronId), new LibraryBranchId(holdAtBranch));
  }

  void extendTo(Instant till) {
    holdTill = till;
  }

  void cancelAt(Instant when) {
    canceledAt = when;
    status = CANCELED;
  }

  void expireAt(Instant when) {
    expiredAt = when;
    status = EXPIRED;
  }

  void checkOutAt(Instant when) {
    checkedOutAt = when;
    status = CHECKED_OUT;
  }
}
