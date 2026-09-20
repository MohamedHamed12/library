package io.pillopl.library.lending.patronprofile.web;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlaceHoldRequest(
    @JsonProperty("bookId") @NotNull(message = "bookId is required") UUID bookId,
    @JsonProperty("libraryBranchId") @NotNull(message = "libraryBranchId is required")
        UUID libraryBranchId,
    @JsonProperty("numberOfDays")
        @Min(value = 1, message = "numberOfDays must be greater than or equal to 1")
        Integer numberOfDays) {

  public UUID getBookId() {
    return bookId;
  }

  public UUID getLibraryBranchId() {
    return libraryBranchId;
  }

  public Integer getNumberOfDays() {
    return numberOfDays;
  }
}
