package io.pillopl.library.lending.patronprofile.infrastructure;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.patronprofile.model.Checkout;

@Entity
@Immutable
@Table(name = "checkouts_sheet")
class PatronCheckoutViewEntity {

  @Id private Long id;

  @Column(name = "book_id")
  private UUID bookId;

  @Column(name = "checked_out_by_patron_id")
  private UUID checkedOutByPatronId;

  @Column(name = "checkout_till")
  private Instant checkoutTill;

  @Column(name = "returned_at")
  private Instant returnedAt;

  protected PatronCheckoutViewEntity() {}

  Checkout toDomainCheckout() {
    return new Checkout(new BookId(bookId), checkoutTill);
  }
}
