package io.pillopl.library.lending.patron.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.vavr.Tuple4;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static io.pillopl.library.lending.patron.model.PlacingOnHoldPolicy.allCurrentPolicies;
import static java.util.stream.Collectors.toSet;

public class PatronFactory {

    public Patron create(PatronType patronType,PatronId patronId,EmailAddress emailAddress,Set<Tuple4<BookId, LibraryBranchId, Instant, Integer>> patronHolds,Map<LibraryBranchId, Set<BookId>> overdueCheckouts,PatronStatus status,String suspensionReason) {
        return new Patron(new PatronInformation(patronId, patronType, emailAddress),
                allCurrentPolicies(),
                new OverdueCheckouts(overdueCheckouts),
                new PatronHolds(
                        patronHolds
                                .stream()
                                .map(tuple -> new Hold(tuple._1, tuple._2, tuple._3, tuple._4))
                                .collect(toSet())),
                status,
                suspensionReason);
    }

}
