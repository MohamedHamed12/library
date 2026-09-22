package io.pillopl.library.lending.patronprofile.web.error;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
    ApiErrorCode code,
    String message,
    String path,
    Instant timestamp,
    List<ApiErrorDetail> details) {

  public ApiErrorCode getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public String getPath() {
    return path;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public List<ApiErrorDetail> getDetails() {
    return details;
  }
}
