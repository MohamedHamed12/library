package io.pillopl.library.lending.patronprofile.web.error;

import lombok.Value;

@Value
public class ApiErrorDetail {

    String field;
    String message;
}