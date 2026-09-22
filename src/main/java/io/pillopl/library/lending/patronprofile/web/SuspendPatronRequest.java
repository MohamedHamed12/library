package io.pillopl.library.lending.patronprofile.web;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SuspendPatronRequest(
    @JsonProperty("reason") @NotBlank(message = "reason is required") String reason) {

  public String getReason() {
    return reason;
  }
}
