package io.pillopl.library.lending.patron.application.patron;

import io.pillopl.library.lending.patron.model.PatronId;

public interface PatronIdGenerator {
    PatronId generate();
}