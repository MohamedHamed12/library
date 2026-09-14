package io.pillopl.library.lending.patronprofile.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.pillopl.library.lending.patron.model.PatronType;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class RegisterPatronRequest {

    @NotNull(message = "type is required")
    PatronType type;

    @NotNull(message = "email is required")
    String email;

    @JsonCreator
    public RegisterPatronRequest(
            @JsonProperty("type") PatronType type,
            @JsonProperty("email") String email
    ) {
        this.type = type;
        this.email = email;
    }
}
