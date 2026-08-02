package io.pillopl.library.lending.patronprofile.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.pillopl.library.lending.patron.model.PatronType;
import javax.validation.constraints.NotNull;
import lombok.Value;

@Value
public class RegisterPatronRequest {

    @NotNull(message = "type is required")
    PatronType type;

    @JsonCreator
    public RegisterPatronRequest(
            @JsonProperty("type") PatronType type
    ) {
        this.type = type;
    }
}