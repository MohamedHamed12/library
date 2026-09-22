package io.pillopl.library.lending.patron.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import io.pillopl.library.lending.patron.model.EmailAddress;
import io.pillopl.library.lending.patron.model.PatronEvent;
import io.pillopl.library.lending.patron.model.PatronEvent.BookCheckedOut;
import io.pillopl.library.lending.patron.model.PatronEvent.BookCheckingOutFailed;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldCanceled;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldCancelingFailed;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldExpired;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldExtended;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldExtensionFailed;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldFailed;
import io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHold;
import io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHoldEvents;
import io.pillopl.library.lending.patron.model.PatronEvent.BookReturned;
import io.pillopl.library.lending.patron.model.PatronEvent.MaximumNumberOhHoldsReached;
import io.pillopl.library.lending.patron.model.PatronEvent.OverdueCheckoutRegistered;
import io.pillopl.library.lending.patron.model.PatronEvent.PatronCreated;
import io.pillopl.library.lending.patron.model.PatronEvent.PatronReactivated;
import io.pillopl.library.lending.patron.model.PatronEvent.PatronSuspended;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.PatronStatus;
import io.pillopl.library.lending.patron.model.PatronType;

@Entity
@Table(name = "patron_database_entity")
class PatronDatabaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "patron_id", unique = true)
  UUID patronId;

  @Enumerated(EnumType.STRING)
  @Column(name = "patron_type", nullable = false, length = 100)
  PatronType patronType;

  @Column(name = "email_address", nullable = false, unique = true, length = 254)
  String emailAddress;

  @Column(name = "status", nullable = false, length = 32)
  String status;

  @Column(name = "suspension_reason", length = 1024)
  String suspensionReason;

  @OneToMany(mappedBy = "patron", cascade = CascadeType.ALL, orphanRemoval = true)
  List<HoldDatabaseEntity> booksOnHold = new ArrayList<>();

  @OneToMany(mappedBy = "patron", cascade = CascadeType.ALL, orphanRemoval = true)
  List<OverdueCheckoutDatabaseEntity> checkouts = new ArrayList<>();

  protected PatronDatabaseEntity() {}

  PatronDatabaseEntity(PatronId patronId, PatronType patronType, EmailAddress emailAddress) {
    this.patronId = patronId.getPatronId();
    this.patronType = patronType;
    this.emailAddress = emailAddress.value();
    this.status = PatronStatus.ACTIVE.name();
  }

  PatronDatabaseEntity handle(PatronEvent event) {
    return switch (event) {
      case BookPlacedOnHoldEvents placedOnHoldEvents -> handle(placedOnHoldEvents);
      case BookPlacedOnHold placedOnHold -> handle(placedOnHold);
      case BookHoldExtended holdExtended -> handle(holdExtended);
      case BookHoldExtensionFailed ignored -> this;
      case BookCheckedOut checkedOut -> handle(checkedOut);
      case BookHoldCanceled holdCanceled -> handle(holdCanceled);
      case BookHoldExpired holdExpired -> handle(holdExpired);
      case OverdueCheckoutRegistered overdueCheckout -> handle(overdueCheckout);
      case BookReturned returned -> handle(returned);
      case PatronSuspended suspended -> handle(suspended);
      case PatronReactivated reactivated -> handle(reactivated);
      case BookHoldFailed ignored -> this;
      case BookCheckingOutFailed ignored -> this;
      case BookHoldCancelingFailed ignored -> this;
      case MaximumNumberOhHoldsReached ignored -> this;
      case PatronCreated ignored ->
          throw new IllegalArgumentException("PatronCreated must create a new patron");
    };
  }

  private PatronDatabaseEntity handle(BookPlacedOnHoldEvents placedOnHoldEvents) {
    return handle(placedOnHoldEvents.getBookPlacedOnHold());
  }

  private PatronDatabaseEntity handle(BookPlacedOnHold event) {
    booksOnHold.add(
        new HoldDatabaseEntity(
            this,
            event.getBookId(),
            event.getPatronId(),
            event.getLibraryBranchId(),
            event.getHoldTill()));
    return this;
  }

  private PatronDatabaseEntity handle(BookHoldExtended event) {
    HoldDatabaseEntity hold =
        booksOnHold.stream()
            .filter(
                entity ->
                    entity.is(event.getPatronId(), event.getBookId(), event.getLibraryBranchId()))
            .findAny()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Cannot extend a hold that is missing from patron state"));
    hold.extendTo(event.getHoldTill(), event.getExtensionCount());
    return this;
  }

  private PatronDatabaseEntity handle(BookHoldCanceled event) {
    return removeHoldIfPresent(event.getPatronId(), event.getBookId(), event.getLibraryBranchId());
  }

  private PatronDatabaseEntity handle(BookCheckedOut event) {
    return removeHoldIfPresent(event.getPatronId(), event.getBookId(), event.getLibraryBranchId());
  }

  private PatronDatabaseEntity handle(BookHoldExpired event) {
    return removeHoldIfPresent(event.getPatronId(), event.getBookId(), event.getLibraryBranchId());
  }

  private PatronDatabaseEntity handle(OverdueCheckoutRegistered event) {
    checkouts.add(
        new OverdueCheckoutDatabaseEntity(
            this, event.getBookId(), event.getPatronId(), event.getLibraryBranchId()));
    return this;
  }

  private PatronDatabaseEntity handle(BookReturned event) {
    return removeOverdueCheckoutIfPresent(
        event.getPatronId(), event.getBookId(), event.getLibraryBranchId());
  }

  private PatronDatabaseEntity handle(PatronSuspended event) {
    status = PatronStatus.SUSPENDED.name();
    suspensionReason = event.getReason();
    return this;
  }

  private PatronDatabaseEntity handle(PatronReactivated event) {
    status = PatronStatus.ACTIVE.name();
    suspensionReason = null;
    return this;
  }

  private PatronDatabaseEntity removeHoldIfPresent(
      UUID patronId, UUID bookId, UUID libraryBranchId) {
    booksOnHold.removeIf(entity -> entity.is(patronId, bookId, libraryBranchId));
    return this;
  }

  private PatronDatabaseEntity removeOverdueCheckoutIfPresent(
      UUID patronId, UUID bookId, UUID libraryBranchId) {
    checkouts.removeIf(entity -> entity.is(patronId, bookId, libraryBranchId));
    return this;
  }
}
