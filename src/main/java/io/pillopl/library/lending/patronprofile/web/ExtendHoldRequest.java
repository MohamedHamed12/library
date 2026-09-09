package io.pillopl.library.lending.patronprofile.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Value;

@Value
public class ExtendHoldRequest {

    @NotNull(message = "additionalDays is required")
    @Min(
        value = 1,
        message = "additionalDays must be greater than or equal to 1"
    )
    Integer additionalDays;

    @JsonCreator
    public ExtendHoldRequest(@JsonProperty("additionalDays") Integer additionalDays) {
        this.additionalDays = additionalDays;
    }
}
