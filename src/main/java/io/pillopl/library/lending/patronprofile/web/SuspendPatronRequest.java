package io.pillopl.library.lending.patronprofile.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class SuspendPatronRequest {

    @NotBlank(message = "reason is required")
    String reason;

    @JsonCreator
    public SuspendPatronRequest(
            @JsonProperty("reason") String reason
    ) {
        this.reason = reason;
    }
}
