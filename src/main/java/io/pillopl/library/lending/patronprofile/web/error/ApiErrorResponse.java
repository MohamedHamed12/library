package io.pillopl.library.lending.patronprofile.web.error;

import java.time.Instant;
import java.util.List;
import lombok.Value;

@Value
public class ApiErrorResponse {

    ApiErrorCode code;
    String message;
    String path;
    Instant timestamp;
    List<ApiErrorDetail> details;
}