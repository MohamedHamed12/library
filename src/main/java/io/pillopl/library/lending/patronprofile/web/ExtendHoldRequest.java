package io.pillopl.library.lending.patronprofile.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExtendHoldRequest(
    @JsonProperty("additionalDays")
        @NotNull(message = "additionalDays is required")
        @Min(value = 1, message = "additionalDays must be greater than or equal to 1")
        Integer additionalDays) {

  public Integer getAdditionalDays() {
    return additionalDays;
  }
}
