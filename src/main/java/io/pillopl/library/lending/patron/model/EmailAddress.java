package io.pillopl.library.lending.patron.model;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public final class EmailAddress {

    private static final int MAX_LENGTH = 254;

    private static final Pattern VALID_EMAIL = Pattern.compile(
            "^[A-Z0-9.!#$%&'*+/=?^_`{|}~-]+@" +
                    "[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?" +
                    "(?:\\.[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?)+$",
            Pattern.CASE_INSENSITIVE);

    private final String value;

    private EmailAddress(String value) {
        this.value = value;
    }

    public static EmailAddress of(String value) {
        if (value == null) {
            throw new InvalidEmailAddress("Email address is required");
        }

        String normalizedValue = value.trim().toLowerCase(Locale.ROOT);

        if (normalizedValue.isEmpty()) {
            throw new InvalidEmailAddress("Email address is required");
        }

        if (normalizedValue.length() > MAX_LENGTH) {
            throw new InvalidEmailAddress("Email address must not exceed 254 characters");
        }

        if (!VALID_EMAIL.matcher(normalizedValue).matches()) {
            throw new InvalidEmailAddress("Email address has an invalid format");
        }

        return new EmailAddress(normalizedValue);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EmailAddress)) {
            return false;
        }
        EmailAddress that = (EmailAddress) other;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}