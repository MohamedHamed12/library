package io.pillopl.library.lending.patronprofile.web;

import io.micrometer.core.instrument.MeterRegistry;
import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.LendingTestContext;
import io.pillopl.library.lending.book.model.BookFixture;
import io.pillopl.library.lending.patron.application.hold.BookNotFoundException;
import io.pillopl.library.lending.patron.application.hold.CancelingHold;
import io.pillopl.library.lending.patron.application.hold.HoldNotFoundException;
import io.pillopl.library.lending.patron.application.hold.PlacingOnHold;
import io.pillopl.library.lending.patron.application.hold.PatronNotFoundException;
import io.pillopl.library.lending.patron.model.PatronFixture;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patronprofile.model.Checkout;
import io.pillopl.library.lending.patronprofile.model.CheckoutsView;
import io.pillopl.library.lending.patronprofile.model.Hold;
import io.pillopl.library.lending.patronprofile.model.HoldsView;
import io.pillopl.library.lending.patronprofile.model.PatronProfile;
import io.pillopl.library.lending.patronprofile.model.PatronProfiles;
import io.vavr.control.Try;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static io.pillopl.library.commons.commands.Result.Rejection;
import static io.pillopl.library.commons.commands.Result.Success;
import static io.vavr.collection.List.of;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(PatronProfileController.class)
@ContextConfiguration(classes = { LendingTestContext.class })
public class PatronProfileControllerIT {

        private final PatronId patronId = PatronFixture.anyPatronId();
        private final BookId bookId = BookFixture.anyBookId();
        private final BookId anotherBook = BookFixture.anyBookId();

        private final Instant anyDate = Instant.parse("2999-01-01T00:00:00Z");

        private final Instant anotherDate = Instant.parse("2999-01-02T00:00:00Z");

        @Autowired
        private MockMvc mvc;

        @MockBean
        private PatronProfiles patronProfiles;

        @MockBean
        private PlacingOnHold placingOnHold;

        @MockBean
        private CancelingHold cancelingHold;

        @MockBean
        private MeterRegistry meterRegistry;

        @Test
        public void shouldReturnPatronProfileSummaryWithCountsAndLinks()
                        throws Exception {

                given(patronProfiles.fetchFor(patronId))
                                .willReturn(profileWithCurrentActivity());

                mvc.perform(get("/profiles/" + patronId.getPatronId())
                                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                                .andExpect(status().isOk())
                                .andExpect(header().string(
                                                CONTENT_TYPE,
                                                MediaTypes.HAL_FORMS_JSON_VALUE))
                                .andExpect(jsonPath(
                                                "$.patronId",
                                                is(patronId.getPatronId().toString())))
                                .andExpect(jsonPath("$.currentHoldsCount", is(1)))
                                .andExpect(jsonPath("$.currentCheckoutsCount", is(1)))
                                .andExpect(jsonPath("$.overdueCheckoutsCount", is(0)))
                                .andExpect(jsonPath(
                                                "$._links.self.href",
                                                containsString(
                                                                "/profiles/" + patronId.getPatronId())))
                                .andExpect(jsonPath(
                                                "$._links.holds.href",
                                                containsString(
                                                                "/profiles/"
                                                                                + patronId.getPatronId()
                                                                                + "/holds")))
                                .andExpect(jsonPath(
                                                "$._links.checkouts.href",
                                                containsString(
                                                                "/profiles/"
                                                                                + patronId.getPatronId()
                                                                                + "/checkouts")));
        }

        @Test
        public void shouldReturnZeroCountsWhenPatronHasNoCurrentActivity()
                        throws Exception {

                given(patronProfiles.fetchFor(patronId))
                                .willReturn(emptyProfile());

                mvc.perform(get("/profiles/" + patronId.getPatronId())
                                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                                .andExpect(status().isOk())
                                .andExpect(header().string(
                                                CONTENT_TYPE,
                                                MediaTypes.HAL_FORMS_JSON_VALUE))
                                .andExpect(jsonPath(
                                                "$.patronId",
                                                is(patronId.getPatronId().toString())))
                                .andExpect(jsonPath("$.currentHoldsCount", is(0)))
                                .andExpect(jsonPath("$.currentCheckoutsCount", is(0)))
                                .andExpect(jsonPath("$.overdueCheckoutsCount", is(0)))
                                .andExpect(jsonPath(
                                                "$._links.self.href",
                                                containsString(
                                                                "/profiles/" + patronId.getPatronId())))
                                .andExpect(jsonPath(
                                                "$._links.holds.href",
                                                containsString(
                                                                "/profiles/"
                                                                                + patronId.getPatronId()
                                                                                + "/holds")))
                                .andExpect(jsonPath(
                                                "$._links.checkouts.href",
                                                containsString(
                                                                "/profiles/"
                                                                                + patronId.getPatronId()
                                                                                + "/checkouts")));
        }

        @Test
        public void shouldCountOnlyOverdueCurrentCheckouts()
                        throws Exception {

                BookId overdueBook = BookFixture.anyBookId();
                BookId futureBook = BookFixture.anyBookId();

                Instant overdueDate = Instant.parse("2000-01-01T00:00:00Z");

                Instant futureDate = Instant.parse("2999-01-01T00:00:00Z");

                PatronProfile profile = new PatronProfile(
                                new HoldsView(of(
                                                new Hold(bookId, anyDate))),
                                new CheckoutsView(of(
                                                new Checkout(overdueBook, overdueDate),
                                                new Checkout(futureBook, futureDate))));

                given(patronProfiles.fetchFor(patronId))
                                .willReturn(profile);

                mvc.perform(get("/profiles/" + patronId.getPatronId())
                                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.currentHoldsCount", is(1)))
                                .andExpect(jsonPath("$.currentCheckoutsCount", is(2)))
                                .andExpect(jsonPath("$.overdueCheckoutsCount", is(1)));
        }

        @Test
        public void shouldReturnEmptySummaryForUnknownPatron()
                        throws Exception {

                PatronId unknownPatronId = PatronFixture.anyPatronId();

                given(patronProfiles.fetchFor(unknownPatronId))
                                .willReturn(emptyProfile());

                mvc.perform(get(
                                "/profiles/" + unknownPatronId.getPatronId()).accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath(
                                                "$.patronId",
                                                is(unknownPatronId.getPatronId().toString())))
                                .andExpect(jsonPath("$.currentHoldsCount", is(0)))
                                .andExpect(jsonPath("$.currentCheckoutsCount", is(0)))
                                .andExpect(jsonPath("$.overdueCheckoutsCount", is(0)))
                                .andExpect(jsonPath(
                                                "$._links.holds.href",
                                                containsString(
                                                                "/profiles/"
                                                                                + unknownPatronId.getPatronId()
                                                                                + "/holds")))
                                .andExpect(jsonPath(
                                                "$._links.checkouts.href",
                                                containsString(
                                                                "/profiles/"
                                                                                + unknownPatronId.getPatronId()
                                                                                + "/checkouts")));
        }

        @Test
        public void shouldCreateLinksForHolds() throws Exception {
                given(patronProfiles.fetchFor(patronId))
                                .willReturn(profileWithCurrentActivity());

                mvc.perform(get(
                                "/profiles/"
                                                + patronId.getPatronId()
                                                + "/holds/")
                                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                                .andExpect(status().isOk())
                                .andExpect(header().string(
                                                CONTENT_TYPE,
                                                MediaTypes.HAL_FORMS_JSON_VALUE))
                                .andExpect(jsonPath(
                                                "$._embedded.holdList[0].bookId",
                                                is(bookId.getBookId().toString())))
                                .andExpect(jsonPath(
                                                "$._embedded.holdList[0]._links.self.href",
                                                containsString(
                                                                "/profiles/"
                                                                                + patronId.getPatronId()
                                                                                + "/holds/"
                                                                                + bookId.getBookId())))
                                .andExpect(jsonPath(
                                                "$._embedded.holdList[0].till",
                                                is(anyDate.toString())))
                                .andExpect(jsonPath(
                                                "$._embedded.holdList[0]._templates.default.method",
                                                is("delete")));
        }

        @Test
        public void shouldCreateLinksForCheckouts() throws Exception {
                given(patronProfiles.fetchFor(patronId))
                                .willReturn(profileWithCurrentActivity());

                mvc.perform(get(
                                "/profiles/"
                                                + patronId.getPatronId()
                                                + "/checkouts/")
                                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                                .andExpect(status().isOk())
                                .andExpect(header().string(
                                                CONTENT_TYPE,
                                                MediaTypes.HAL_FORMS_JSON_VALUE))
                                .andExpect(jsonPath(
                                                "$._embedded.checkoutList[0].bookId",
                                                is(anotherBook.getBookId().toString())))
                                .andExpect(jsonPath(
                                                "$._embedded.checkoutList[0].till",
                                                is(anotherDate.toString())))
                                .andExpect(jsonPath(
                                                "$._embedded.checkoutList[0]._links.self.href",
                                                containsString(
                                                                "/profiles/"
                                                                                + patronId.getPatronId()
                                                                                + "/checkouts/"
                                                                                + anotherBook.getBookId())));
        }

        @Test
        public void shouldReturn404WhenThereIsNoHold()
                        throws Exception {

                given(patronProfiles.fetchFor(patronId))
                                .willReturn(profileWithCurrentActivity());

                String path = "/profiles/"
                                + patronId.getPatronId()
                                + "/holds/"
                                + UUID.randomUUID();

                mvc.perform(get(path)
                                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath(
                                                "$.code",
                                                is("HOLD_NOT_FOUND")))
                                .andExpect(jsonPath(
                                                "$.message",
                                                is("The requested hold was not found.")))
                                .andExpect(jsonPath("$.path", is(path)))
                                .andExpect(jsonPath("$.timestamp").exists())
                                .andExpect(jsonPath("$.details").isArray());
        }

        @Test
        public void shouldReturn404WhenThereIsNoCheckout()
                        throws Exception {

                given(patronProfiles.fetchFor(patronId))
                                .willReturn(profileWithCurrentActivity());

                String path = "/profiles/"
                                + patronId.getPatronId()
                                + "/checkouts/"
                                + UUID.randomUUID();

                mvc.perform(get(path)
                                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath(
                                                "$.code",
                                                is("CHECKOUT_NOT_FOUND")))
                                .andExpect(jsonPath(
                                                "$.message",
                                                is("The requested checkout was not found.")))
                                .andExpect(jsonPath("$.path", is(path)))
                                .andExpect(jsonPath("$.timestamp").exists())
                                .andExpect(jsonPath("$.details").isArray());
        }

        @Test
        public void shouldReturnResourceForHold() throws Exception {
                given(patronProfiles.fetchFor(patronId))
                                .willReturn(profileWithCurrentActivity());

                mvc.perform(get(
                                "/profiles/"
                                                + patronId.getPatronId()
                                                + "/holds/"
                                                + bookId.getBookId())
                                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                                .andExpect(status().isOk())
                                .andExpect(header().string(
                                                CONTENT_TYPE,
                                                MediaTypes.HAL_FORMS_JSON_VALUE))
                                .andExpect(jsonPath(
                                                "$.bookId",
                                                is(bookId.getBookId().toString())))
                                .andExpect(jsonPath(
                                                "$.till",
                                                is(anyDate.toString())))
                                .andExpect(jsonPath(
                                                "$._templates.default.method",
                                                is("delete")))
                                .andExpect(jsonPath(
                                                "$._links.self.href",
                                                containsString(
                                                                "/profiles/"
                                                                                + patronId.getPatronId()
                                                                                + "/holds/"
                                                                                + bookId.getBookId())));
        }

        @Test
        public void shouldReturnResourceForCheckout()
                        throws Exception {

                given(patronProfiles.fetchFor(patronId))
                                .willReturn(profileWithCurrentActivity());

                mvc.perform(get(
                                "/profiles/"
                                                + patronId.getPatronId()
                                                + "/checkouts/"
                                                + anotherBook.getBookId())
                                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                                .andExpect(status().isOk())
                                .andExpect(header().string(
                                                CONTENT_TYPE,
                                                MediaTypes.HAL_FORMS_JSON_VALUE))
                                .andExpect(jsonPath(
                                                "$.bookId",
                                                is(anotherBook.getBookId().toString())))
                                .andExpect(jsonPath(
                                                "$.till",
                                                is(anotherDate.toString())))
                                .andExpect(jsonPath(
                                                "$._links.self.href",
                                                containsString(
                                                                "/profiles/"
                                                                                + patronId.getPatronId()
                                                                                + "/checkouts/"
                                                                                + anotherBook.getBookId())));
        }

        @Test
        public void shouldReturn500IfSomethingFailedWhileDuringPlacingOnHold()
                        throws Exception {

                given(placingOnHold.placeOnHold(any()))
                                .willReturn(Try.failure(
                                                new IllegalArgumentException()));

                String request = "{"
                                + "\"bookId\":\"6e1dfec5-5cfe-487e-814e-d70114f5396e\","
                                + "\"libraryBranchId\":"
                                + "\"a518e2ef-5f6c-43e3-a7fc-5d895e15be3a\","
                                + "\"numberOfDays\":1"
                                + "}";

                mvc.perform(post(
                                "/profiles/"
                                                + patronId.getPatronId()
                                                + "/holds")
                                .accept(MediaTypes.HAL_FORMS_JSON_VALUE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        public void shouldCancelExistingHold() throws Exception {
                given(patronProfiles.fetchFor(patronId))
                                .willReturn(profileWithCurrentActivity());

                given(cancelingHold.cancelHold(any()))
                                .willReturn(Try.success(Success));

                mvc.perform(delete(
                                "/profiles/"
                                                + patronId.getPatronId()
                                                + "/holds/"
                                                + bookId.getBookId())
                                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                                .andExpect(status().isNoContent());
        }

        @Test
        public void shouldNotCancelNotExistingHold()
                        throws Exception {

                given(patronProfiles.fetchFor(patronId))
                                .willReturn(profileWithCurrentActivity());

                given(cancelingHold.cancelHold(any()))
                                .willReturn(Try.failure(
                                                new HoldNotFoundException(bookId)));

                mvc.perform(delete(holdPath())
                                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath(
                                                "$.code",
                                                is("HOLD_NOT_FOUND")))
                                .andExpect(jsonPath(
                                                "$.message",
                                                is("The requested hold was not found.")));
        }

        @Test
        public void shouldReturn500IfSomethingFailedWhileCanceling()
                        throws Exception {

                given(patronProfiles.fetchFor(patronId))
                                .willReturn(profileWithCurrentActivity());

                given(cancelingHold.cancelHold(any()))
                                .willReturn(Try.failure(
                                                new IllegalStateException()));

                mvc.perform(delete(
                                "/profiles/"
                                                + patronId.getPatronId()
                                                + "/holds/"
                                                + bookId.getBookId())
                                .accept(MediaTypes.HAL_FORMS_JSON_VALUE))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath(
                                                "$.code",
                                                is("INTERNAL_ERROR")))
                                .andExpect(jsonPath(
                                                "$.message",
                                                is("An unexpected error occurred.")))
                                .andExpect(jsonPath("$.exception").doesNotExist())
                                .andExpect(jsonPath("$.trace").doesNotExist());
        }

        @Test
        public void shouldReturn404WhenBookDoesNotExist()
                        throws Exception {

                given(placingOnHold.placeOnHold(any()))
                                .willReturn(Try.failure(
                                                new BookNotFoundException(bookId)));

                mvc.perform(post(placeHoldPath())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validPlaceHoldRequest()))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath(
                                                "$.code",
                                                is("BOOK_NOT_FOUND")))
                                .andExpect(jsonPath(
                                                "$.message",
                                                is("The requested book was not found.")))
                                .andExpect(jsonPath("$.details").isArray());
        }

        @Test
        public void shouldReturn404WhenPatronDoesNotExist()
                        throws Exception {

                given(placingOnHold.placeOnHold(any()))
                                .willReturn(Try.failure(
                                                new PatronNotFoundException(patronId)));

                mvc.perform(post(placeHoldPath())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validPlaceHoldRequest()))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath(
                                                "$.code",
                                                is("PATRON_NOT_FOUND")))
                                .andExpect(jsonPath(
                                                "$.message",
                                                is("The requested patron was not found.")));
        }

        @Test
        public void shouldReturn404WhenCancelingUnknownHold()
                        throws Exception {

                given(cancelingHold.cancelHold(any()))
                                .willReturn(Try.failure(
                                                new HoldNotFoundException(bookId)));

                mvc.perform(delete(holdPath()))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath(
                                                "$.code",
                                                is("HOLD_NOT_FOUND")))
                                .andExpect(jsonPath(
                                                "$.message",
                                                is("The requested hold was not found.")));
        }

        @Test
        public void shouldReturn409WhenHoldCancellationIsRejected()
                        throws Exception {

                given(cancelingHold.cancelHold(any()))
                                .willReturn(Try.success(Rejection));

                mvc.perform(delete(holdPath()))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath(
                                                "$.code",
                                                is("HOLD_CANCELLATION_NOT_ALLOWED")))
                                .andExpect(jsonPath(
                                                "$.message",
                                                is("The hold cannot be canceled in its current state.")));
        }

        @Test
        public void shouldReturnGeneric500WhenCancelingUnexpectedlyFails()
                        throws Exception {

                given(cancelingHold.cancelHold(any()))
                                .willReturn(Try.failure(
                                                new IllegalStateException(
                                                                "Internal implementation detail")));

                mvc.perform(delete(holdPath()))
                                .andExpect(status().isInternalServerError())
                                .andExpect(jsonPath(
                                                "$.code",
                                                is("INTERNAL_ERROR")))
                                .andExpect(jsonPath(
                                                "$.message",
                                                is("An unexpected error occurred.")))
                                .andExpect(jsonPath("$.exception").doesNotExist())
                                .andExpect(jsonPath("$.trace").doesNotExist());
        }

        private PatronProfile profileWithCurrentActivity() {
                return new PatronProfile(
                                new HoldsView(of(
                                                new Hold(bookId, anyDate))),
                                new CheckoutsView(of(
                                                new Checkout(anotherBook, anotherDate))));
        }

        private PatronProfile emptyProfile() {
                return new PatronProfile(
                                new HoldsView(of()),
                                new CheckoutsView(of()));
        }

        private String placeHoldPath() {
                return "/profiles/" + patronId.getPatronId() + "/holds";
        }

        private String holdPath() {
                return "/profiles/" + patronId.getPatronId() + "/holds/" + bookId.getBookId();
        }

        private String validPlaceHoldRequest() {
                return placeHoldRequestWithNumberOfDays(1);
        }

        private String placeHoldRequestWithNumberOfDays(int numberOfDays) {
                return "{"
                                + "\"bookId\":\"6e1dfec5-5cfe-487e-814e-d70114f5396e\","
                                + "\"libraryBranchId\":"
                                + "\"a518e2ef-5f6c-43e3-a7fc-5d895e15be3a\","
                                + "\"numberOfDays\":" + numberOfDays
                                + "}";
        }

        @Test
        public void shouldPlaceBookOnHold() throws Exception {
                given(placingOnHold.placeOnHold(any()))
                                .willReturn(Try.success(Success));
                mvc.perform(post(placeHoldPath())
                                .accept(MediaTypes.HAL_FORMS_JSON_VALUE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(placeHoldRequestWithNumberOfDays(1)))
                                .andExpect(status().isOk());
        }

        @Test
        public void shouldAllowMissingNumberOfDays() throws Exception {
                given(placingOnHold.placeOnHold(any()))
                                .willReturn(Try.success(Success));

                String request = "{"
                                + "\"bookId\":\"6e1dfec5-5cfe-487e-814e-d70114f5396e\","
                                + "\"libraryBranchId\":"
                                + "\"a518e2ef-5f6c-43e3-a7fc-5d895e15be3a\""
                                + "}";
                mvc.perform(post(placeHoldPath())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                                .andExpect(status().isOk());
        }

        @Test
        public void shouldReturn400WhenBookIdIsMissing() throws Exception {
                String request = "{"
                                + "\"libraryBranchId\":"
                                + "\"a518e2ef-5f6c-43e3-a7fc-5d895e15be3a\","
                                + "\"numberOfDays\":1"
                                + "}";

                mvc.perform(post(placeHoldPath())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                                .andExpect(jsonPath("$.details[0].field", is("bookId")))
                                .andExpect(jsonPath("$.path", is(placeHoldPath())))
                                .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        public void shouldReturn400WhenLibraryBranchIdIsMissing()
                        throws Exception {

                String request = "{"
                                + "\"bookId\":\"6e1dfec5-5cfe-487e-814e-d70114f5396e\","
                                + "\"numberOfDays\":1"
                                + "}";

                mvc.perform(post(placeHoldPath())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                                .andExpect(jsonPath(
                                                "$.details[0].field",
                                                is("libraryBranchId")));
        }

        @Test
        public void shouldReturn400WhenNumberOfDaysIsZero()
                        throws Exception {

                mvc.perform(post(placeHoldPath())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(placeHoldRequestWithNumberOfDays(0)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                                .andExpect(jsonPath(
                                                "$.details[0].field",
                                                is("numberOfDays")));
        }

        @Test
        public void shouldReturn400WhenNumberOfDaysIsNegative()
                        throws Exception {

                mvc.perform(post(placeHoldPath())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(placeHoldRequestWithNumberOfDays(-1)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                                .andExpect(jsonPath(
                                                "$.details[0].field",
                                                is("numberOfDays")));
        }

        @Test
        public void shouldReturn400ForMalformedRequestBody()
                        throws Exception {

                String request = "{"
                                + "\"bookId\":\"not-a-uuid\","
                                + "\"libraryBranchId\":"
                                + "\"a518e2ef-5f6c-43e3-a7fc-5d895e15be3a\""
                                + "}";

                mvc.perform(post(placeHoldPath())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code", is("MALFORMED_REQUEST")))
                                .andExpect(jsonPath(
                                                "$.message",
                                                is("The request body is malformed.")))
                                .andExpect(jsonPath("$.details").isArray());
        }

        @Test
        public void shouldReturn400ForInvalidPathUuid() throws Exception {

                mvc.perform(get("/profiles/not-a-uuid"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath(
                                                "$.code",
                                                is("INVALID_PATH_PARAMETER")))
                                .andExpect(jsonPath("$.path", is("/profiles/not-a-uuid")));
        }

        @Test
        public void shouldReturn409WhenHoldIsRejectedByDomainPolicy()
                        throws Exception {

                given(placingOnHold.placeOnHold(any()))
                                .willReturn(Try.success(Rejection));
                mvc.perform(post(placeHoldPath())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validPlaceHoldRequest()))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.code", is("HOLD_NOT_ALLOWED")))
                                .andExpect(jsonPath(
                                                "$.message",
                                                is("The patron cannot place this book on hold.")));
        }
}