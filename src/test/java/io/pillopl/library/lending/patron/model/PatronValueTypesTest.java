package io.pillopl.library.lending.patron.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class PatronValueTypesTest {

  @Test
  void patronInformationKeepsValueSemantics() {
    PatronId patronId = new PatronId(UUID.randomUUID());
    EmailAddress emailAddress = EmailAddress.of("patron@example.test");

    PatronInformation information =
        new PatronInformation(patronId, PatronType.Regular, emailAddress);
    PatronInformation sameInformation =
        new PatronInformation(new PatronId(patronId.getPatronId()), PatronType.Regular, emailAddress);

    assertTrue(PatronInformation.class.isRecord());
    assertEquals(information, sameInformation);
    assertEquals(information.hashCode(), sameInformation.hashCode());
    assertEquals(patronId, information.getPatronId());
    assertEquals(PatronType.Regular, information.getType());
    assertEquals(emailAddress, information.getEmailAddress());
    assertTrue(information.isRegular());
  }

  @Test
  void patronInformationKeepsNullInvariants() {
    PatronId patronId = new PatronId(UUID.randomUUID());
    EmailAddress emailAddress = EmailAddress.of("patron@example.test");

    assertThrows(
        NullPointerException.class,
        () -> new PatronInformation(null, PatronType.Regular, emailAddress));
    assertThrows(
        NullPointerException.class, () -> new PatronInformation(patronId, null, emailAddress));
    assertThrows(
        NullPointerException.class,
        () -> new PatronInformation(patronId, PatronType.Regular, null));
  }

  @Test
  void rejectionKeepsValueSemantics() {
    Rejection rejection = Rejection.withReason("rejected");
    Rejection sameRejection = Rejection.withReason("rejected");

    assertTrue(Rejection.class.isRecord());
    assertTrue(Rejection.Reason.class.isRecord());
    assertEquals(rejection, sameRejection);
    assertEquals(rejection.hashCode(), sameRejection.hashCode());
    assertEquals("rejected", rejection.getReason().getReason());
  }

  @Test
  void rejectionKeepsNullInvariants() {
    assertThrows(NullPointerException.class, () -> new Rejection(null));
    assertThrows(NullPointerException.class, () -> new Rejection.Reason(null));
  }
}
