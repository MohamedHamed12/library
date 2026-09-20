package io.pillopl.library.lending.patronprofile.web;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.pillopl.library.lending.patron.model.PatronType;

public record RegisterPatronRequest(
    @JsonProperty("type") @NotNull(message = "type is required") PatronType type,
    @JsonProperty("email") @NotNull(message = "email is required") String email) {

  public PatronType getType() {
    return type;
  }

  public String getEmail() {
    return email;
  }
}
