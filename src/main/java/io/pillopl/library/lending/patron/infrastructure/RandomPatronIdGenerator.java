package io.pillopl.library.lending.patron.infrastructure;

import io.pillopl.library.lending.patron.application.patron.PatronIdGenerator;
import io.pillopl.library.lending.patron.model.PatronId;
import java.util.UUID;

public class RandomPatronIdGenerator implements PatronIdGenerator {
    @Override
    public PatronId generate() {
        return new PatronId(UUID.randomUUID());
    }
}