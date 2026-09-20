package io.pillopl.library.lending.patron.model;

import java.util.UUID;

import io.pillopl.library.lending.PatronReference;

public class PatronId extends PatronReference {

  public PatronId(UUID patronId) {
    super(patronId);
  }

  @Override
  public String toString() {
    return "PatronId(patronId=" + getPatronId() + ")";
  }
}
