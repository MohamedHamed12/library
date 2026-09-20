package io.pillopl.library;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.pillopl.library.lending.patron.application.checkout.CheckOutBookCommand;
import io.pillopl.library.lending.patron.application.hold.CancelHoldCommand;
import io.pillopl.library.lending.patron.application.hold.ExtendHoldCommand;
import io.pillopl.library.lending.patron.application.patron.ReactivatePatronCommand;
import io.pillopl.library.lending.patron.application.patron.RegisterPatronCommand;
import io.pillopl.library.lending.patron.application.patron.SuspendPatronCommand;

class ApplicationCommandRecordsTest {

  @Test
  void simpleApplicationCommandsAreRecords() {
    assertTrue(CancelHoldCommand.class.isRecord());
    assertTrue(ExtendHoldCommand.class.isRecord());
    assertTrue(CheckOutBookCommand.class.isRecord());
    assertTrue(RegisterPatronCommand.class.isRecord());
    assertTrue(SuspendPatronCommand.class.isRecord());
    assertTrue(ReactivatePatronCommand.class.isRecord());
  }
}
