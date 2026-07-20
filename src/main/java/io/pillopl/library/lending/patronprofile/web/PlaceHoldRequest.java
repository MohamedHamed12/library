package io.pillopl.library.lending.patronprofile.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Value;

@Value
public class PlaceHoldRequest {

    @NotNull(message = "bookId is required")
    UUID bookId;

    @NotNull(message = "libraryBranchId is required")
    UUID libraryBranchId;

    @Min(
        value = 1,
        message = "numberOfDays must be greater than or equal to 1"
    )
    Integer numberOfDays;

    @JsonCreator
    public PlaceHoldRequest(
        @JsonProperty("bookId") UUID bookId,
        @JsonProperty("libraryBranchId") UUID libraryBranchId,
        @JsonProperty("numberOfDays") Integer numberOfDays
    ) {
        this.bookId = bookId;
        this.libraryBranchId = libraryBranchId;
        this.numberOfDays = numberOfDays;
    }
}