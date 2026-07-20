package io.pillopl.library.lending.patronprofile.web.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final ApiErrorCode code;

    public ApiException(HttpStatus status, ApiErrorCode code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static ApiException conflict(ApiErrorCode code, String message) {
        return new ApiException(
                HttpStatus.CONFLICT,
                code,
                message);
    }

    public static ApiException notFound(ApiErrorCode code, String message) {
        return new ApiException(
                HttpStatus.NOT_FOUND,
                code,
                message);
    }
}

