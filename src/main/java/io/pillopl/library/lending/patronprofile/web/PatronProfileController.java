package io.pillopl.library.lending.patronprofile.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.micrometer.core.annotation.Timed;
import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.application.hold.CancelHoldCommand;
import io.pillopl.library.lending.patron.application.hold.CancelingHold;
import io.pillopl.library.lending.patron.application.hold.PlaceOnHoldCommand;
import io.pillopl.library.lending.patron.application.hold.PlacingOnHold;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patronprofile.model.PatronProfiles;
import io.pillopl.library.lending.patronprofile.model.PatronProfile;
import io.vavr.control.Option;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import io.pillopl.library.lending.patronprofile.web.error.ApiException;
import javax.validation.Valid;
import io.pillopl.library.lending.patronprofile.web.error.ApiException;
import javax.validation.Valid;

import static io.pillopl.library.lending.patronprofile.web.error.ApiErrorCode.HOLD_NOT_ALLOWED;
import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.ResponseEntity.ok;
import io.pillopl.library.lending.patronprofile.web.error.ApiErrorCode;
import io.pillopl.library.lending.patronprofile.web.error.ApiException;

@Timed(percentiles = { 0.5, 0.75, 0.95, 0.99 })
@RestController
@AllArgsConstructor
class PatronProfileController {

        private final PatronProfiles patronProfiles;
        private final PlacingOnHold placingOnHold;
        private final CancelingHold cancelingHold;

        @GetMapping("/profiles/{patronId}")
        ResponseEntity<PatronProfileSummaryResource> patronProfile(
                        @PathVariable UUID patronId) {

                PatronProfile profile = patronProfiles.fetchFor(new PatronId(patronId));

                Instant now = Instant.now();

                int currentHoldsCount = profile
                                .getHoldsView()
                                .getCurrentHolds()
                                .size();

                int currentCheckoutsCount = profile
                                .getCurrentCheckouts()
                                .getCurrentCheckouts()
                                .size();

                int overdueCheckoutsCount = profile
                                .getCurrentCheckouts()
                                .getCurrentCheckouts()
                                .filter(checkout -> checkout.getTill().isBefore(now))
                                .size();

                return ok(new PatronProfileSummaryResource(
                                patronId,
                                currentHoldsCount,
                                currentCheckoutsCount,
                                overdueCheckoutsCount));
        }

        @GetMapping("/profiles/{patronId}/holds/")
        ResponseEntity<CollectionModel<EntityModel<Hold>>> findHolds(@PathVariable UUID patronId) {
                List<EntityModel<Hold>> holds = patronProfiles.fetchFor(new PatronId(patronId))
                                .getHoldsView()
                                .getCurrentHolds()
                                .toStream()
                                .map(hold -> resourceWithLinkToHoldSelf(patronId, hold))
                                .collect(toList());
                return ResponseEntity.ok(new CollectionModel<>(holds,
                                linkTo(methodOn(PatronProfileController.class).findHolds(patronId)).withSelfRel()));

        }

        @GetMapping("/profiles/{patronId}/holds/{bookId}")
        ResponseEntity<EntityModel<Hold>> findHold(@PathVariable UUID patronId, @PathVariable UUID bookId) {
                return patronProfiles.fetchFor(new PatronId(patronId))
                                .findHold(new BookId(bookId))
                                .map(hold -> ok(resourceWithLinkToHoldSelf(patronId, hold)))
                                .getOrElseThrow(() -> ApiException.notFound(
                                                ApiErrorCode.HOLD_NOT_FOUND,
                                                "The requested hold was not found."));

        }

        @GetMapping("/profiles/{patronId}/checkouts/")
        ResponseEntity<CollectionModel<EntityModel<Checkout>>> findCheckouts(@PathVariable UUID patronId) {
                List<EntityModel<Checkout>> checkouts = patronProfiles.fetchFor(new PatronId(patronId))
                                .getCurrentCheckouts()
                                .getCurrentCheckouts()
                                .toStream()
                                .map(checkout -> resourceWithLinkToCheckoutSelf(patronId, checkout))
                                .collect(toList());
                return ResponseEntity.ok(new CollectionModel<>(checkouts,
                                linkTo(methodOn(PatronProfileController.class).findHolds(patronId)).withSelfRel()));
        }

        @GetMapping("/profiles/{patronId}/checkouts/{bookId}")
        ResponseEntity<EntityModel<Checkout>> findCheckout(@PathVariable UUID patronId, @PathVariable UUID bookId) {
                return patronProfiles
                                .fetchFor(new PatronId(patronId))
                                .findCheckout(new BookId(bookId))
                                .map(checkout -> ok(resourceWithLinkToCheckoutSelf(
                                                patronId,
                                                checkout)))
                                .getOrElseThrow(() -> ApiException.notFound(
                                                ApiErrorCode.CHECKOUT_NOT_FOUND,
                                                "The requested checkout was not found."));
        }

        // @PostMapping("/profiles/{patronId}/holds")
        // ResponseEntity<Void> placeHold(@PathVariable UUID patronId, @Valid
        // @RequestBody PlaceHoldRequest request) {
        // Result result = placingOnHold.placeOnHold(new PlaceOnHoldCommand(
        // Instant.now(),
        // new PatronId(patronId),
        // new LibraryBranchId(
        // request.getLibraryBranchId()),
        // new BookId(request.getBookId()),
        // Option.of(request.getNumberOfDays())))
        // .get();

        // if (result == Result.Rejection) {
        // throw ApiException.conflict(
        // HOLD_NOT_ALLOWED,
        // "The patron cannot place this book on hold.");
        // }

        // return ResponseEntity.ok().build();
        // }
        @PostMapping("/profiles/{patronId}/holds")
        ResponseEntity<Void> placeHold(@PathVariable UUID patronId, @Valid @RequestBody PlaceHoldRequest request) {
                Result result = placingOnHold
                                .placeOnHold(
                                                new PlaceOnHoldCommand(
                                                                Instant.now(),
                                                                new PatronId(patronId),
                                                                new LibraryBranchId(
                                                                                request.getLibraryBranchId()),
                                                                new BookId(request.getBookId()),
                                                                Option.of(request.getNumberOfDays())))
                                .get();

                rejectIfNeeded(
                                result,
                                ApiErrorCode.HOLD_NOT_ALLOWED,
                                "The patron cannot place this book on hold.");

                return ResponseEntity.ok().build();
        }

        @DeleteMapping("/profiles/{patronId}/holds/{bookId}")
        ResponseEntity<Void> cancelHold(@PathVariable UUID patronId, @PathVariable UUID bookId) {
                Result result = cancelingHold
                                .cancelHold(
                                                new CancelHoldCommand(
                                                                Instant.now(),
                                                                new PatronId(patronId),
                                                                new BookId(bookId)))
                                .get();

                rejectIfNeeded(
                                result,
                                ApiErrorCode.HOLD_CANCELLATION_NOT_ALLOWED,
                                "The hold cannot be canceled in its current state.");

                return ResponseEntity.noContent().build();
        }

        private EntityModel<Hold> resourceWithLinkToHoldSelf(UUID patronId,
                        io.pillopl.library.lending.patronprofile.model.Hold hold) {
                return new EntityModel<>(
                                new Hold(hold),
                                linkTo(methodOn(PatronProfileController.class).findHold(patronId,
                                                hold.getBook().getBookId()))
                                                .withSelfRel()
                                                .andAffordance(afford(methodOn(PatronProfileController.class)
                                                                .cancelHold(patronId, hold.getBook().getBookId()))));
        }

        private EntityModel<Checkout> resourceWithLinkToCheckoutSelf(UUID patronId,
                        io.pillopl.library.lending.patronprofile.model.Checkout checkout) {
                return new EntityModel<>(
                                new Checkout(checkout),
                                linkTo(methodOn(PatronProfileController.class).findCheckout(patronId,
                                                checkout.getBook().getBookId()))
                                                .withSelfRel());
        }

        private void rejectIfNeeded(Result result, ApiErrorCode code, String message) {
                if (result == Result.Rejection) {
                        throw ApiException.conflict(code, message);
                }
        }
}

@Value
class PatronProfileSummaryResource
                extends RepresentationModel<PatronProfileSummaryResource> {

        UUID patronId;
        int currentHoldsCount;
        int currentCheckoutsCount;
        int overdueCheckoutsCount;

        PatronProfileSummaryResource(
                        UUID patronId,
                        int currentHoldsCount,
                        int currentCheckoutsCount,
                        int overdueCheckoutsCount) {

                this.patronId = patronId;
                this.currentHoldsCount = currentHoldsCount;
                this.currentCheckoutsCount = currentCheckoutsCount;
                this.overdueCheckoutsCount = overdueCheckoutsCount;
                add(linkTo(methodOn(PatronProfileController.class)
                                .findHolds(patronId))
                                .withRel("holds"));

                add(linkTo(methodOn(PatronProfileController.class)
                                .findCheckouts(patronId))
                                .withRel("checkouts"));

                add(linkTo(methodOn(PatronProfileController.class)
                                .patronProfile(patronId))
                                .withSelfRel());
        }
}

@Value
class Hold {

        UUID bookId;
        Instant till;

        Hold(io.pillopl.library.lending.patronprofile.model.Hold hold) {
                this.bookId = hold.getBook().getBookId();
                this.till = hold.getTill();
        }
}

@Value
class Checkout {

        UUID bookId;
        Instant till;

        Checkout(io.pillopl.library.lending.patronprofile.model.Checkout hold) {
                this.bookId = hold.getBook().getBookId();
                this.till = hold.getTill();
        }
}
