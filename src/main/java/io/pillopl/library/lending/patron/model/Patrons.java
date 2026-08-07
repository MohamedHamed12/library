package io.pillopl.library.lending.patron.model;

import io.vavr.control.Option;

public interface Patrons {

    Option<Patron> findBy(PatronId patronId);

    boolean existsBy(EmailAddress emailAddress);

    Patron publish(PatronEvent event);
}
