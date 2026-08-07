package io.pillopl.library.lending.patron.model;

public class InvalidEmailAddress extends IllegalArgumentException {

    public InvalidEmailAddress(String message) {
        super(message);
    }
}