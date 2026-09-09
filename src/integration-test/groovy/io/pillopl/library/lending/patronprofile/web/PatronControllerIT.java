package io.pillopl.library.lending.patronprofile.web;

import io.micrometer.core.instrument.MeterRegistry;
import io.pillopl.library.lending.LendingTestContext;
import io.pillopl.library.lending.patron.application.patron.ReactivatingPatron;
import io.pillopl.library.lending.patron.application.patron.RegisteringPatron;
import io.pillopl.library.lending.patron.application.patron.SuspendingPatron;
import io.pillopl.library.lending.patron.model.EmailAddress;
import io.pillopl.library.lending.patron.model.EmailAddressAlreadyRegistered;
import io.pillopl.library.lending.patron.model.PatronFixture;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatronController.class)
@ContextConfiguration(classes = { LendingTestContext.class })
public class PatronControllerIT {

        private final PatronId patronId = PatronFixture.anyPatronId();
        private final java.time.Instant fixedNow = java.time.Instant.parse("2999-01-01T00:00:00Z");

        @Autowired
        private MockMvc mvc;

        @MockitoBean
        private RegisteringPatron registeringPatron;

        @MockitoBean
        private SuspendingPatron suspendingPatron;

        @MockitoBean
        private ReactivatingPatron reactivatingPatron;

        @MockitoBean
        private Clock clock;

        @MockitoBean
        private MeterRegistry meterRegistry;

        @BeforeEach
        public void setUp() {
                given(clock.instant()).willReturn(fixedNow);
        }

        @Test
        public void shouldRegisterRegularPatronAndReturn201WithLocation() throws Exception {
                given(registeringPatron.register(any()))
                                .willReturn(Try.success(patronId));

                String request = "{" +
                                "\"type\":\"Regular\"," +
                                "\"email\":\"regular@example.test\"" +
                                "}";

                mvc.perform(post("/patrons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                                .andExpect(status().isCreated())
                                .andExpect(header().string("Location", containsString("/profiles/" + patronId.getPatronId())));
        }

        @Test
        public void shouldRegisterResearcherPatronAndReturn201WithLocation() throws Exception {
                given(registeringPatron.register(any()))
                                .willReturn(Try.success(patronId));

                String request = "{" +
                                "\"type\":\"Researcher\"," +
                                "\"email\":\"researcher@example.test\"" +
                                "}";

                mvc.perform(post("/patrons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                                .andExpect(status().isCreated())
                                .andExpect(header().string("Location", containsString("/profiles/" + patronId.getPatronId())));
        }

        @Test
        public void shouldReturn400WhenTypeIsMissing() throws Exception {
                String request = "{" +
                                "\"email\":\"nobody@example.test\"" +
                                "}";

                mvc.perform(post("/patrons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                                .andExpect(jsonPath("$.details[0].field", is("type")));
        }

        @Test
        public void shouldReturn400WhenEmailIsMissing() throws Exception {
                String request = "{" +
                                "\"type\":\"Regular\"" +
                                "}";

                mvc.perform(post("/patrons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                                .andExpect(jsonPath("$.details[0].field", is("email")));
        }

        @Test
        public void shouldReturn400WhenEmailIsInvalid() throws Exception {
                given(registeringPatron.register(any()))
                                .willReturn(Try.success(patronId));

                String request = "{" +
                                "\"type\":\"Regular\"," +
                                "\"email\":\"not-an-email\"" +
                                "}";

                mvc.perform(post("/patrons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code", is("INVALID_EMAIL_ADDRESS")))
                                .andExpect(jsonPath("$.details[0].field", is("email")));
        }

        @Test
        public void shouldReturn409WhenEmailIsAlreadyRegistered() throws Exception {
                given(registeringPatron.register(any()))
                                .willReturn(Try.failure(new EmailAddressAlreadyRegistered(EmailAddress.of("duplicate@example.test"))));

                String request = "{" +
                                "\"type\":\"Regular\"," +
                                "\"email\":\"duplicate@example.test\"" +
                                "}";

                mvc.perform(post("/patrons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.code", is("EMAIL_ADDRESS_ALREADY_REGISTERED")));
        }

        @Test
        public void shouldReturn400WhenTypeIsInvalid() throws Exception {
                String request = "{" +
                                "\"type\":\"INVALID\"," +
                                "\"email\":\"nobody@example.test\"" +
                                "}";

                mvc.perform(post("/patrons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code", is("MALFORMED_REQUEST")));
        }
}
