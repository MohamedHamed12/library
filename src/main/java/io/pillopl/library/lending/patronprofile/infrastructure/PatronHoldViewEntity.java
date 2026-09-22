package io.pillopl.library.lending.patronprofile.infrastructure;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.patronprofile.model.Hold;

@Entity
@Immutable
@Table(name = "holds_sheet")
class PatronHoldViewEntity {

  @Id
  private Long id;

  @Column(name = "book_id")
  private UUID bookId;

  @Column(name = "hold_by_patron_id")
  private UUID holdByPatronId;

  @Column(name = "hold_till")
  private Instant holdTill;

  @Column(name = "checked_out_at")
  private Instant checkedOutAt;

  @Column(name = "expired_at")
  private Instant expiredAt;

  @Column(name = "canceled_at")
  private Instant canceledAt;

  protected PatronHoldViewEntity() {}

  Hold toDomainHold() {
    return new Hold(new BookId(bookId), holdTill);
  }
}
