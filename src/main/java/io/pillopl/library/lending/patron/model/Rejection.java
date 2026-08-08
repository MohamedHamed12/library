package io.pillopl.library.lending.patron.model;

import lombok.NonNull;
import lombok.Value;

@Value
public class Rejection {

    @Value
    public static class Reason {
        @NonNull
        String reason;
    }

    @NonNull
    Reason reason;

    static Rejection withReason(String reason) {
        return new Rejection(new Reason(reason));
    }
}
