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
import io.pillopl.library.lending.dailysheet.model.OverdueCheckout;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent.BookCheckedOut;
import io.pillopl.library.lending.patron.model.PatronEvent.BookReturned;
import io.pillopl.library.lending.patron.model.PatronId;

@Entity
@Table(name = "checkouts_sheet")
class CheckoutSheetEntity {

  static final String CHECKED_OUT = "CHECKEDOUT";
  static final String RETURNED = "RETURNED";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "book_id", nullable = false)
  private UUID bookId;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "checkout_event_id", unique = true)
  private UUID checkoutEventId;

  @Column(name = "checked_out_by_patron_id")
  private UUID checkedOutByPatronId;

  @Column(name = "checked_out_at")
  private Instant checkedOutAt;

  @Column(name = "returned_at")
  private Instant returnedAt;

  @Column(name = "checked_out_at_branch")
  private UUID checkedOutAtBranch;

  @Column(name = "checkout_till")
  private Instant checkoutTill;

  protected CheckoutSheetEntity() {}

  private CheckoutSheetEntity(BookCheckedOut event) {
    bookId = event.getBookId();
    status = CHECKED_OUT;
    checkoutEventId = event.getEventId();
    checkedOutByPatronId = event.getPatronId();
    checkedOutAt = event.getWhen();
    checkedOutAtBranch = event.getLibraryBranchId();
    checkoutTill = event.getTill();
  }

  private CheckoutSheetEntity(BookReturned event) {
    bookId = event.getBookId();
    status = CHECKED_OUT;
    checkoutEventId = event.getEventId();
    checkedOutByPatronId = event.getPatronId();
    returnedAt = event.getWhen();
  }

  static CheckoutSheetEntity from(BookCheckedOut event) {
    return new CheckoutSheetEntity(event);
  }

  static CheckoutSheetEntity returnedWithoutCheckout(BookReturned event) {
    return new CheckoutSheetEntity(event);
  }

  OverdueCheckout toOverdueCheckout() {
    return new OverdueCheckout(
        new BookId(bookId),
        new PatronId(checkedOutByPatronId),
        new LibraryBranchId(checkedOutAtBranch));
  }

  void returnAt(Instant when) {
    returnedAt = when;
    status = RETURNED;
  }
}
