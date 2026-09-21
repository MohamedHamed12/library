package io.pillopl.library.lending.patron.model;

import java.util.Optional;

public interface Patrons {

  Optional<Patron> findBy(PatronId patronId);

  boolean existsBy(EmailAddress emailAddress);

  Patron publish(PatronEvent event);
}
