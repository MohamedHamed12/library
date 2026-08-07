package io.pillopl.library.lending.patron.model;

public class EmailAddressAlreadyRegistered extends RuntimeException {

    private final EmailAddress emailAddress;

    public EmailAddressAlreadyRegistered(EmailAddress emailAddress) {
        super("Email address is already registered: " + emailAddress.value());
        this.emailAddress = emailAddress;
    }

    public EmailAddress getEmailAddress() {
        return emailAddress;
    }
}