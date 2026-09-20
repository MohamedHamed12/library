package io.pillopl.library;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.pillopl.library.lending.patronprofile.web.ExtendHoldRequest;
import io.pillopl.library.lending.patronprofile.web.PlaceHoldRequest;
import io.pillopl.library.lending.patronprofile.web.RegisterPatronRequest;
import io.pillopl.library.lending.patronprofile.web.SuspendPatronRequest;

class WebRequestRecordsTest {

  @Test
  void immutableWebRequestsAreRecords() {
    assertTrue(ExtendHoldRequest.class.isRecord());
    assertTrue(PlaceHoldRequest.class.isRecord());
    assertTrue(RegisterPatronRequest.class.isRecord());
    assertTrue(SuspendPatronRequest.class.isRecord());
  }
}
