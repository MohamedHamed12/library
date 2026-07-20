package io.pillopl.library.lending.patron.application.hold;

import io.pillopl.library.lending.patron.model.PatronId;

public class PatronNotFoundException extends RuntimeException {

    public PatronNotFoundException(PatronId patronId) {
        super(
            "Patron with given Id does not exist: "
                + patronId.getPatronId()
        );
    }
}