package io.pillopl.library.lending.patronprofile.web.error;

public enum ApiErrorCode {

    VALIDATION_FAILED,
    MALFORMED_REQUEST,
    INVALID_PATH_PARAMETER,

    PATRON_NOT_FOUND,
    BOOK_NOT_FOUND,
    HOLD_NOT_FOUND,
    CHECKOUT_NOT_FOUND,

    HOLD_NOT_ALLOWED,
    HOLD_CANCELLATION_NOT_ALLOWED,

    INTERNAL_ERROR
}