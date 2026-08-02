package io.pillopl.library.lending.patronprofile.web;

import io.micrometer.core.instrument.MeterRegistry;
import io.pillopl.library.lending.LendingTestContext;
import io.pillopl.library.lending.patron.application.patron.RegisteringPatron;
import io.pillopl.library.lending.patron.model.PatronFixture;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Try;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
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

@RunWith(SpringRunner.class)
@WebMvcTest(PatronController.class)
@ContextConfiguration(classes = { LendingTestContext.class })
public class PatronControllerIT {

        private final PatronId patronId = PatronFixture.anyPatronId();
        private final java.time.Instant fixedNow = java.time.Instant.parse("2999-01-01T00:00:00Z");

        @Autowired
        private MockMvc mvc;

        @MockBean
        private RegisteringPatron registeringPatron;

        @MockBean
        private Clock clock;

        @MockBean
        private MeterRegistry meterRegistry;

        @org.junit.Before
        public void setUp() {
                given(clock.instant()).willReturn(fixedNow);
        }

        @Test
        public void shouldRegisterRegularPatronAndReturn201WithLocation() throws Exception {
                given(registeringPatron.register(any()))
                                .willReturn(Try.success(patronId));

                String request = "{" +
                                "\"name\":\"Ada Lovelace\"," +
                                "\"type\":\"Regular\"" +
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
                                "\"name\":\"Alan Turing\"," +
                                "\"type\":\"Researcher\"" +
                                "}";

                mvc.perform(post("/patrons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                                .andExpect(status().isCreated())
                                .andExpect(header().string("Location", containsString("/profiles/" + patronId.getPatronId())));
        }

        @Test
        public void shouldReturn400WhenNameIsMissing() throws Exception {
                String request = "{" +
                                "\"type\":\"Regular\"" +
                                "}";

                mvc.perform(post("/patrons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                                .andExpect(jsonPath("$.details[0].field", is("name")));
        }

        @Test
        public void shouldReturn400WhenTypeIsMissing() throws Exception {
                String request = "{" +
                                "\"name\":\"Ada Lovelace\"" +
                                "}";

                mvc.perform(post("/patrons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code", is("VALIDATION_FAILED")))
                                .andExpect(jsonPath("$.details[0].field", is("type")));
        }

        @Test
        public void shouldReturn400WhenTypeIsInvalid() throws Exception {
                String request = "{" +
                                "\"name\":\"Ada Lovelace\"," +
                                "\"type\":\"INVALID\"" +
                                "}";

                mvc.perform(post("/patrons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code", is("MALFORMED_REQUEST")));
        }
}