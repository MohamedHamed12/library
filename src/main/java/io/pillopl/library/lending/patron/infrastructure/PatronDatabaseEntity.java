package io.pillopl.library.lending.patron.infrastructure;

import io.pillopl.library.lending.patron.model.EmailAddress;
import io.pillopl.library.lending.patron.model.PatronEvent;
import io.pillopl.library.lending.patron.model.PatronEvent.*;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.PatronStatus;
import io.pillopl.library.lending.patron.model.PatronType;
import io.vavr.API;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
class PatronDatabaseEntity {

    @Id
    Long id;
    UUID patronId;
    PatronType patronType;
    String emailAddress;
    String status;
    String suspensionReason;
    Set<HoldDatabaseEntity> booksOnHold;
    Set<OverdueCheckoutDatabaseEntity> checkouts;

     PatronDatabaseEntity(PatronId patronId, PatronType patronType,EmailAddress emailAddress) {
        this.patronId = patronId.getPatronId();
        this.patronType = patronType;
        this.emailAddress = emailAddress.value();
        this.status = PatronStatus.ACTIVE.name();
        this.suspensionReason = null;
        this.booksOnHold = new HashSet<>();
        this.checkouts = new HashSet<>();
    }

    PatronDatabaseEntity handle(PatronEvent event) {
        return API.Match(event).of(
                Case($(instanceOf(BookPlacedOnHoldEvents.class)), this::handle),
                Case($(instanceOf(BookPlacedOnHold.class)), this::handle),
                Case($(instanceOf(BookHoldExtended.class)), this::handle),
                Case($(instanceOf(BookHoldExtensionFailed.class)), this::handle),
                Case($(instanceOf(BookCheckedOut.class)), this::handle),
                Case($(instanceOf(BookHoldCanceled.class)), this::handle),
                Case($(instanceOf(BookHoldExpired.class)), this::handle),
                Case($(instanceOf(OverdueCheckoutRegistered.class)), this::handle),
                Case($(instanceOf(BookReturned.class)), this::handle),
                Case($(instanceOf(PatronSuspended.class)), this::handle),
                Case($(instanceOf(PatronReactivated.class)), this::handle)
        );
    }

    private PatronDatabaseEntity handle(BookPlacedOnHoldEvents placedOnHoldEvents) {
        BookPlacedOnHold event = placedOnHoldEvents.getBookPlacedOnHold();
        return handle(event);
    }

    private PatronDatabaseEntity handle(BookPlacedOnHold event) {
        booksOnHold.add(new HoldDatabaseEntity(event.getBookId(), event.getPatronId(), event.getLibraryBranchId(), event.getHoldTill()));
        return this;
    }

    private PatronDatabaseEntity handle(BookHoldExtended event) {
        HoldDatabaseEntity hold = booksOnHold
                .stream()
                .filter(entity -> entity.is(event.getPatronId(), event.getBookId(), event.getLibraryBranchId()))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Cannot extend a hold that is missing from patron state"));
        hold.extendTo(event.getHoldTill(), event.getExtensionCount());
        return this;
    }

    private PatronDatabaseEntity handle(BookHoldExtensionFailed event) {
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
        checkouts.add(new OverdueCheckoutDatabaseEntity(event.getBookId(), event.getPatronId(), event.getLibraryBranchId()));
        return this;
    }

    private PatronDatabaseEntity handle(BookReturned event) {
        return removeOverdueCheckoutIfPresent(event.getPatronId(), event.getBookId(), event.getLibraryBranchId());
    }

    private PatronDatabaseEntity handle(PatronSuspended event) {
        this.status = PatronStatus.SUSPENDED.name();
        this.suspensionReason = event.getReason();
        return this;
    }

    private PatronDatabaseEntity handle(PatronReactivated event) {
        this.status = PatronStatus.ACTIVE.name();
        this.suspensionReason = null;
        return this;
    }

    private PatronDatabaseEntity removeHoldIfPresent(UUID patronId, UUID bookId, UUID libraryBranchId) {
        booksOnHold
                .stream()
                .filter(entity -> entity.is(patronId, bookId, libraryBranchId))
                .findAny()
                .ifPresent(entity -> booksOnHold.remove(entity));
        return this;
    }

    private PatronDatabaseEntity removeOverdueCheckoutIfPresent(UUID patronId, UUID bookId, UUID libraryBranchId) {
        checkouts
                .stream()
                .filter(entity -> entity.is(patronId, bookId, libraryBranchId))
                .findAny()
                .ifPresent(entity -> checkouts.remove(entity));
        return this;
    }

}


