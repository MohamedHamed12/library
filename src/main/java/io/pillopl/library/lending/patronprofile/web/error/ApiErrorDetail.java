package io.pillopl.library.lending.patronprofile.web.error;

public record ApiErrorDetail(String field, String message) {

  public String getField() {
    return field;
  }

  public String getMessage() {
    return message;
  }
}
