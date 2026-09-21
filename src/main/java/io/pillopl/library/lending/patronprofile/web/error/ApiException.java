package io.pillopl.library.lending.patronprofile.web.error;

import java.util.Objects;

import org.springframework.http.HttpStatus;

public final class ApiException extends RuntimeException {

  private final HttpStatus status;
  private final ApiErrorCode code;

  public ApiException(HttpStatus status, ApiErrorCode code, String message) {
    super(message);
    this.status = Objects.requireNonNull(status, "status");
    this.code = Objects.requireNonNull(code, "code");
  }

  public HttpStatus getStatus() {
    return status;
  }

  public ApiErrorCode getCode() {
    return code;
  }

  public static ApiException conflict(ApiErrorCode code, String message) {
    return new ApiException(HttpStatus.CONFLICT, code, message);
  }

  public static ApiException notFound(ApiErrorCode code, String message) {
    return new ApiException(HttpStatus.NOT_FOUND, code, message);
  }
}
