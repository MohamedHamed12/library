# DDD Library Fork: 20-Issue Java, Spring, and DDD Learning Backlog

This backlog is designed for a personal fork of `ddd-by-examples/library`. The issues are ordered so that each stage builds on concepts introduced earlier.

## How to use this backlog

1. Fork the repository.
2. Create these issues in your fork, in the listed order.
3. Use one branch and one pull request per issue.
4. Do not combine issues unless an issue explicitly depends on unfinished work.
5. For every pull request, include:
   - the business behavior added or changed;
   - the DDD concept practiced;
   - the Spring or Java concept practiced;
   - tests executed and their results;
   - any design trade-off you made.

## Suggested labels

- `learning/java`
- `learning/spring`
- `learning/ddd`
- `domain-model`
- `application-layer`
- `infrastructure`
- `cqrs`
- `events`
- `testing`
- `architecture`
- `beginner`, `intermediate`, `advanced`

## Dependency roadmap

```text
1 -> 2 -> 3 -> 4 -> 5
               |
               +-> 6 -> 7 -> 8 -> 9 -> 10

1-10 -> 11 -> 12 -> 13 -> 14 -> 15 -> 16 -> 17 -> 18

All earlier issues -> 19 -> 20
```

---

# Issue 1 - Add a Combined Patron Profile Summary Endpoint

**Type:** Enhancement  
**Difficulty:** Beginner  
**Suggested labels:** `learning/java`, `learning/spring`, `cqrs`, `beginner`  
**Depends on:** None

## Learning objectives

- Understand an existing Spring MVC controller.
- Practice immutable response DTOs.
- Reuse the existing CQRS read model without loading an aggregate.
- Practice HATEOAS links and HTTP response construction.
- Learn the difference between a query and a command.

## Context

`PatronProfileController` currently exposes separate endpoints for a patron's holds and checkouts. The profile root only returns the patron identifier and links. Add a useful summary representation that combines the current read-side data without mutating domain state.

## Requested behavior

Enhance:

```http
GET /profiles/{patronId}
```

The response must contain:

- `patronId`;
- `currentHoldsCount`;
- `currentCheckoutsCount`;
- `overdueCheckoutsCount`, when the current read model exposes enough information to calculate it safely;
- links to the holds and checkouts collections.

If the current read model cannot reliably calculate overdue count, add a clearly named query method to the read-model interface rather than reaching into persistence from the controller.

## Implementation tasks

- Inspect `PatronProfileController`, `PatronProfiles`, and the profile read-model implementation.
- Introduce a dedicated response resource such as `PatronProfileSummaryResource`.
- Keep query composition outside the domain aggregate.
- Do not call `Patrons.findBy` or rebuild the `Patron` aggregate for this endpoint.
- Preserve the existing holds and checkouts endpoints.
- Return an empty summary with zero counts when a valid patron has no current holds or checkouts.
- Decide and document whether an unknown patron returns `404` or an empty profile. Base the decision on the existing read-model semantics and apply it consistently.

## Suggested investigation scope

- `src/main/java/io/pillopl/library/lending/patronprofile/web/PatronProfileController.java`
- `src/main/java/io/pillopl/library/lending/patronprofile/model/`
- `src/main/java/io/pillopl/library/lending/patronprofile/infrastructure/`
- Existing Patron Profile unit and integration tests

## Acceptance criteria

- `GET /profiles/{patronId}` returns both hold and checkout counts.
- The endpoint is read-only and does not publish domain events.
- The controller does not query SQL directly.
- Existing links remain valid.
- The response uses a dedicated DTO/resource rather than exposing persistence entities.
- Existing profile endpoints remain backward compatible.

## Testing

- Add a test for a patron with no holds or checkouts.
- Add a test for a patron with at least one hold and one checkout.
- Add a test for the chosen unknown-patron behavior.
- Verify the HATEOAS links.
- Run the relevant existing unit and integration tests.

## Out of scope

- Authentication and authorization.
- Changing the hold or checkout business rules.
- Adding a new database table.

## Definition of done

- The endpoint behavior is documented with an example response.
- Tests prove the counts and links.
- No command-side aggregate is loaded for the query.
- The pull request explains why this belongs to the read side of CQRS.

---

# Issue 2 - Introduce Request Validation and a Consistent REST Error Model

**Type:** Refactor and enhancement  
**Difficulty:** Beginner to intermediate  
**Suggested labels:** `learning/spring`, `application-layer`, `testing`, `beginner`  
**Depends on:** Issue 1

## Learning objectives

- Practice Bean Validation and Spring MVC validation.
- Learn `@ControllerAdvice` and exception handling.
- Distinguish malformed requests, missing resources, business rejections, and technical failures.
- Avoid returning HTTP 500 for expected business outcomes.

## Context

The current controller manually converts `Try<Result>` values into responses and maps many failures to `500 Internal Server Error`. Request DTOs do not clearly express validation constraints. This makes expected rejections hard for API clients to understand.

## Requested behavior

Create a consistent error representation, for example:

```json
{
  "code": "HOLD_NOT_ALLOWED",
  "message": "The patron cannot place this book on hold.",
  "path": "/profiles/.../holds",
  "timestamp": "...",
  "details": []
}
```

Use appropriate status codes:

- `400 Bad Request` for malformed input or invalid request fields;
- `404 Not Found` when the requested patron, book, or hold is absent;
- `409 Conflict` for a valid command rejected because of current domain state;
- `500 Internal Server Error` only for unexpected technical failures.

## Business and API rules

- `bookId` is required when placing a hold.
- `libraryBranchId` is required.
- `numberOfDays`, when supplied, must be positive.
- Do not expose exception stack traces, SQL text, or internal class names in API responses.
- Domain rejection details should be translated at the web boundary; do not add HTTP concepts to the domain model.

## Implementation tasks

- Add validation annotations to request DTOs where suitable.
- Add `@Valid` at controller boundaries.
- Introduce a central error handler using `@ControllerAdvice` or an equivalent Spring mechanism.
- Introduce a stable API error code enum or constants at the web layer.
- Map existing command results and domain failures to explicit HTTP outcomes.
- Remove duplicated error-response construction from controller methods.
- Preserve the current domain method signatures unless a small change is required to expose an existing rejection safely.

## Suggested investigation scope

- `PatronProfileController`
- `PlaceHoldRequest`
- `PlacingOnHold`
- `CancelingHold`
- `Result`
- Patron hold failure events and exceptions

## Acceptance criteria

- Invalid UUID/path/request values produce a structured `400` response.
- Missing holds or books produce `404` where appropriate.
- Expected business rejection produces `409`, not `500`.
- Unexpected exceptions still produce a generic `500` response.
- The domain and application packages do not depend on Spring MVC or HTTP classes.
- All API error responses follow one JSON structure.

## Testing

- Missing `bookId`.
- Missing `libraryBranchId`.
- Negative and zero `numberOfDays`.
- Unknown hold cancellation.
- Hold rejected by a domain policy.
- Simulated unexpected application exception.
- Assert both status code and error body.

## Out of scope

- Internationalization.
- OAuth or security.
- Rewriting all application errors in one large refactor.

## Definition of done

- Error mappings are documented.
- Controller methods are simpler than before.
- Expected failures no longer appear as internal server errors.
- Tests cover validation, business rejection, not-found, and technical failure.

---

# Issue 3 - Introduce a Clock Port and Remove Direct Calls to `Instant.now()`

**Type:** Refactor  
**Difficulty:** Beginner to intermediate  
**Suggested labels:** `learning/java`, `learning/spring`, `domain-model`, `testing`  
**Depends on:** Issue 2

## Learning objectives

- Learn dependency inversion for time.
- Practice Java's `Clock` API or a small domain-specific time provider.
- Understand deterministic tests.
- Learn Spring bean configuration without contaminating domain classes.

## Context

The web controller currently creates commands with `Instant.now()`. Time-dependent behavior also exists in hold expiration and overdue checkout processing. Direct time access makes tests harder and lets different layers choose time independently.

## Requested behavior

Introduce one explicit source of current time. Preferred options:

1. Inject `java.time.Clock` into application services/controllers and call `Instant.now(clock)`; or
2. Define a small port such as `CurrentTimeProvider` in an application-facing package and implement it in infrastructure.

Choose one approach and explain the trade-off in the pull request.

## Implementation tasks

- Register a production UTC clock/time provider as a Spring bean.
- Replace direct `Instant.now()` calls in the use cases touched by this issue.
- Pass time into domain operations through commands or method parameters.
- Do not inject Spring beans directly into aggregate roots or value objects.
- Introduce fixed-clock tests for hold placement, cancellation, expiration, and overdue logic where relevant.
- Ensure application behavior does not depend on the machine's local time zone.

## Business rules and constraints

- Existing time-based business semantics must remain unchanged.
- Persisted event timestamps must come from the command/use-case time source.
- A single command must use one consistent timestamp throughout its execution.

## Suggested investigation scope

- `PatronProfileController`
- Hold and checkout commands
- `ExpiringHolds`
- Overdue checkout registration
- Daily Sheet scheduled processing
- Spring configuration classes in Lending

## Acceptance criteria

- No direct `Instant.now()` remains in the controller methods changed by this issue.
- Tests can set a fixed instant without sleeping.
- Expiration boundary tests are deterministic.
- Domain classes remain independent of Spring.
- Production uses UTC.

## Testing

- Hold created at a fixed instant.
- Closed-ended hold expiration immediately before, at, and after the boundary.
- Checkout overdue boundary.
- Verify all events produced by one command use the expected timestamp.

## Out of scope

- User-specific time zones.
- Calendar/holiday calculations.
- Changing hold duration rules.

## Definition of done

- Time is an explicit dependency.
- No test uses `Thread.sleep` for time behavior.
- The pull request contains a short note describing why time is infrastructure/application concern rather than aggregate state.

---

# Issue 4 - Add a Patron Registration Use Case and REST Endpoint

**Type:** Feature  
**Difficulty:** Intermediate  
**Suggested labels:** `learning/spring`, `learning/ddd`, `application-layer`, `domain-model`  
**Depends on:** Issues 2 and 3

## Learning objectives

- Implement a complete vertical slice: controller -> command -> application service -> domain event -> repository -> read model.
- Understand aggregate creation and factories.
- Practice domain-event publication.
- Learn where identifier generation belongs.

## Context

The domain already contains `Patron`, `PatronFactory`, `PatronInformation`, `PatronType`, `PatronId`, and a `PatronCreated` event, but the public API is focused on profile operations. Add a clear patron-registration workflow.

## Requested API

```http
POST /patrons
Content-Type: application/json
```

Example request:

```json
{
  "name": "Ada Lovelace",
  "type": "REGULAR"
}
```

Example success:

```http
201 Created
Location: /profiles/{generatedPatronId}
```

The exact fields in the request must follow the existing `PatronInformation` model. Do not invent personal data fields that the current domain does not need.

## Business rules

- Patron identifier is generated by the application or an injected ID generator, not accepted blindly from the caller.
- Patron type must be one of the supported domain values.
- Required patron information cannot be blank.
- Registration produces `PatronCreated`.
- A successfully registered patron can immediately be loaded by the command-side repository.
- The profile read model must eventually or immediately expose the patron according to the configured consistency model.

## Implementation tasks

- Add `RegisterPatronCommand`.
- Add an application service such as `RegisteringPatron`.
- Use `PatronFactory` or improve it rather than duplicating aggregate construction.
- Add an ID-generation port if generation is not already centralized.
- Publish `PatronCreated` through `Patrons`/domain events using the repository's established style.
- Add a REST controller or extend an appropriate existing web package.
- Return `201 Created` and a profile location link.
- Keep DTOs out of the domain package.

## Acceptance criteria

- A valid request creates a regular or researcher patron.
- The response contains the generated patron ID or a location link.
- Invalid patron type is rejected with the standard error model from Issue 2.
- Registration uses the clock from Issue 3.
- Registration publishes exactly one logical `PatronCreated` event.
- Existing hold and checkout behavior is unchanged.

## Testing

- Register a regular patron.
- Register a researcher patron.
- Reject missing/blank required information.
- Reject unsupported patron type.
- Verify the aggregate can be loaded after registration.
- Verify the profile endpoint behavior after registration.
- Verify event timestamp and ID generation with deterministic test doubles.

## Out of scope

- Authentication accounts.
- Passwords.
- Patron deletion.
- Duplicate-email rules; those are introduced in Issue 5.

## Definition of done

- The full vertical slice is implemented and tested.
- The pull request includes a sequence diagram or short flow description.
- Domain construction is not performed in the controller.

---

# Issue 5 - Model Patron Email as an Immutable `EmailAddress` Value Object

**Type:** Refactor and enhancement  
**Difficulty:** Intermediate  
**Suggested labels:** `learning/java`, `learning/ddd`, `domain-model`, `testing`  
**Depends on:** Issue 4

## Learning objectives

- Practice value-object design.
- Learn immutability, equality, validation, and persistence mapping.
- Understand the difference between domain validation and request validation.
- Practice safe database migration in a small project.

## Context

Add an email address to patron registration and model it as a value object rather than a raw `String`. This issue is intentionally focused on Java and tactical DDD rather than advanced email-delivery features.

## Business rules

- An email address is required for newly registered patrons.
- Leading and trailing whitespace is removed before validation.
- The normalized value is used for equality and uniqueness.
- Domain validation should reject clearly invalid values.
- Do not attempt to fully implement every RFC email edge case. Use a documented, reasonable validation strategy.
- Email comparison for uniqueness is case-insensitive for the whole value in this learning project.
- Existing seeded patrons must be migrated or assigned documented fixture emails.

## Implementation tasks

- Add immutable `EmailAddress` in the Patron domain model.
- Add construction through a factory method or validating constructor.
- Prevent invalid instances from existing.
- Add email to `PatronInformation`.
- Update registration request mapping.
- Update persistence entities and database schema.
- Add repository support for checking uniqueness without exposing SQL to the domain.
- Define an application/domain rejection for duplicate email.
- Ensure JSON DTOs use strings and translate at the boundary.

## Acceptance criteria

- `EmailAddress` has value-based equality and hash code.
- Invalid addresses cannot be constructed.
- Registration rejects duplicate normalized email with `409 Conflict`.
- Persistence round-trips the value correctly.
- Domain classes do not contain validation annotations from Spring MVC.
- Existing tests and fixtures are updated.

## Testing

- Valid email construction.
- Leading/trailing whitespace normalization.
- Case-insensitive equality/uniqueness.
- Missing `@`, missing local part, missing domain, blank value.
- Registration with duplicate email.
- Persistence mapping test.
- JSON request/response mapping test.

## Out of scope

- Email verification links.
- Sending email.
- Multiple email addresses per patron.
- Internationalized email addresses.

## Definition of done

- Raw email strings do not enter the aggregate.
- The domain model uses `EmailAddress` consistently.
- The pull request explains why this concept is a value object and not an entity.

---

# Issue 6 - Add Patron Suspension and Reactivation

**Type:** Feature  
**Difficulty:** Intermediate  
**Suggested labels:** `learning/ddd`, `domain-model`, `application-layer`, `testing`  
**Depends on:** Issues 4 and 5

## Learning objectives

- Add lifecycle state to an aggregate.
- Protect invariants inside the aggregate rather than in controllers.
- Model commands and past-tense domain events.
- Understand when a policy belongs to the aggregate.

## Context

Libraries sometimes temporarily prevent a patron from borrowing books without deleting the patron. Add explicit suspension and reactivation behavior.

## Business rules

- A patron can be `ACTIVE` or `SUSPENDED`.
- New patrons start as `ACTIVE`.
- Only an active patron can place a hold or check out a book.
- A suspended patron may still view their profile and return an already checked-out book.
- Suspending an already suspended patron is rejected as an idempotent conflict or treated as no-op; choose one behavior and document it.
- Reactivating an active patron follows the same consistency choice.
- Suspension does not automatically cancel existing holds or checkouts in this issue.
- Suspension requires a non-blank reason for auditability.

## Requested API

```http
POST /patrons/{patronId}/suspension
{
  "reason": "Repeated policy violations"
}
```

```http
DELETE /patrons/{patronId}/suspension
```

## Implementation tasks

- Introduce a domain concept such as `PatronStatus`.
- Add `SuspendPatronCommand` and `ReactivatePatronCommand`.
- Add application services for both operations.
- Add `PatronSuspended` and `PatronReactivated` events.
- Update `Patron` to enforce the new invariant before hold/checkout behavior.
- Persist the status and reason where appropriate.
- Update Patron Profile projection to expose status.
- Add web endpoints and map expected rejections using the API error model.

## Acceptance criteria

- New patrons are active.
- Active patrons can be suspended.
- Suspended patrons cannot place holds or check out books.
- Suspended patrons can return books and view profiles.
- Reactivation restores hold/checkout eligibility, subject to all existing policies.
- The status is visible from the profile read model.
- No web or Spring classes are introduced into the domain model.

## Testing

- Suspend active patron.
- Reject or safely handle duplicate suspension according to the documented decision.
- Reject blank suspension reason.
- Hold attempt by suspended patron.
- Checkout attempt by suspended patron.
- Return by suspended patron.
- Reactivate and place a valid hold.
- Projection and persistence tests.

## Out of scope

- Roles/permissions for librarians.
- Automatic suspension based on overdue count.
- Canceling existing holds.

## Definition of done

- The aggregate protects suspension rules.
- Events use past-tense domain language.
- The PR explains why suspension belongs to Patron rather than a web filter.

---

# Issue 7 - Add a Hold Extension Use Case

**Type:** Feature  
**Difficulty:** Intermediate  
**Suggested labels:** `learning/ddd`, `domain-model`, `application-layer`, `events`  
**Depends on:** Issues 3 and 6

## Learning objectives

- Extend an existing aggregate behavior without bypassing invariants.
- Practice value objects and date calculations.
- Model a business rejection using `Either` or the repository's established result style.
- Update a CQRS projection from a domain event.

## Context

Patrons can place closed-ended or open-ended holds, but there is no explicit operation to extend an existing closed-ended hold.

## Business rules

- Only the patron who owns the hold can extend it.
- Only a current, closed-ended hold can be extended.
- An expired, canceled, completed, or open-ended hold cannot be extended.
- The extension is expressed as a positive number of additional days.
- A regular patron may extend a hold once, by at most 7 days.
- A researcher patron may extend a hold up to two times, each by at most 14 days.
- Extension is calculated from the current `till` date, not from the request time.
- An extension must not convert a closed-ended hold into an open-ended hold.

## Requested API

```http
POST /profiles/{patronId}/holds/{bookId}/extension
{
  "additionalDays": 5
}
```

## Implementation tasks

- Add a value object such as `HoldExtension` or reuse `NumberOfDays` only if its semantics remain clear.
- Track extension count as part of the Patron aggregate state required to enforce the rule.
- Add `ExtendHoldCommand` and `ExtendingHold` application service.
- Add `BookHoldExtended` and a clear failure outcome/event.
- Update Patron persistence mapping.
- Update Daily Sheet so the old expiration entry is replaced with the new date.
- Update Patron Profile so the new `till` date is visible.
- Add the REST endpoint and error mappings.

## Acceptance criteria

- Valid regular and researcher extensions succeed within their limits.
- Invalid extension days are rejected.
- Extension count rules are enforced by the aggregate.
- Daily Sheet contains only the new effective expiration date.
- Patron Profile reflects the extension.
- Existing hold policies continue to work.

## Testing

- Regular patron first extension.
- Regular patron second extension rejection.
- Researcher first and second extension.
- Researcher third extension rejection.
- Extension beyond maximum days.
- Extension of open-ended, expired, canceled, and unknown hold.
- Boundary test using the fixed clock.
- Projection update tests.

## Out of scope

- Librarian overrides.
- Changing maximum hold counts.
- Notification emails.

## Definition of done

- Extension rules live in domain behavior.
- Both write model and read models remain consistent.
- Tests describe the rules in business language.

---

# Issue 8 - Add Checkout Renewal with a Domain Policy

**Type:** Feature  
**Difficulty:** Intermediate to advanced  
**Suggested labels:** `learning/ddd`, `domain-model`, `events`, `testing`  
**Depends on:** Issues 3, 6, and 7

## Learning objectives

- Model a domain policy around an existing checkout lifecycle.
- Distinguish a checkout renewal from creating a new checkout.
- Coordinate Patron, Book, Daily Sheet, and Patron Profile using events.
- Practice state-based and policy-based validation.

## Context

A patron can check out and return a book, but cannot renew the checkout period.

## Business rules

- Only the patron who owns a current checkout can renew it.
- Suspended patrons cannot renew.
- An overdue checkout cannot be renewed.
- A checkout can be renewed only once.
- The renewal adds 14 days for a regular patron and 30 days for a researcher patron.
- The resulting total checkout duration from the original checkout date must never exceed 60 days.
- A restricted book cannot be renewed.
- Renewal does not create a second checkout record.

## Requested API

```http
POST /profiles/{patronId}/checkouts/{bookId}/renewal
```

## Implementation tasks

- Identify which aggregate owns the renewal decision and document the reason.
- Add `RenewCheckoutCommand` and `RenewingCheckout`.
- Add a named policy rather than adding all rules as controller conditions.
- Add `BookCheckoutRenewed` and a clear rejection result/event.
- Persist renewal count and original checkout time if not already available.
- Update Daily Sheet overdue date.
- Update Patron Profile checkout `till` date.
- Keep Book state as `CheckedOutBook`; renewal changes dates, not the state type.

## Acceptance criteria

- Valid renewal extends the checkout exactly once.
- Overdue, restricted, suspended, unknown, or already-renewed cases are rejected.
- Total duration never exceeds 60 days.
- The Daily Sheet uses the renewed due date.
- No duplicate checkout projection is created.

## Testing

- Regular patron renewal.
- Researcher patron renewal.
- Second renewal rejection.
- Renewal that would exceed 60 days.
- Overdue checkout rejection.
- Restricted book rejection.
- Suspended patron rejection.
- Read-model and persistence updates.

## Out of scope

- Multiple configurable renewal plans.
- Fines.
- Reservations by another patron blocking renewal; that can be a future extension.

## Definition of done

- The rule set is represented by named domain concepts.
- No controller contains business-rule branching.
- Projection tests prove the old due date is replaced.

---

# Issue 9 - Introduce a `LostBook` State and Reporting Flow

**Type:** Feature  
**Difficulty:** Advanced  
**Suggested labels:** `learning/java`, `learning/ddd`, `domain-model`, `events`  
**Depends on:** Issue 8

## Learning objectives

- Extend the type-state model used by `AvailableBook`, `BookOnHold`, and `CheckedOutBook`.
- Use the Java type system to prevent invalid transitions.
- Coordinate a state change with Patron and read models.
- Learn how persistence maps a new domain subtype/state.

## Context

The Book model represents lifecycle states with separate classes. Add a lost-book workflow while preserving this design.

## Business rules

- Only a currently checked-out book can be reported lost.
- The patron reporting the loss must own the checkout.
- Reporting a book lost completes/removes the active checkout from the patron's current checkout list.
- A lost book is not available, cannot be placed on hold, and cannot be checked out.
- Returning a lost book is not allowed through the normal return command.
- A separate recovery operation can restore a lost book to `AvailableBook`.
- Recovery is a librarian/system operation and requires a reason or note.

## Requested operations

```http
POST /profiles/{patronId}/checkouts/{bookId}/lost
```

```http
POST /books/{bookId}/recovery
{
  "note": "Found during inventory"
}
```

## Implementation tasks

- Add `LostBook` as a domain type.
- Add `BookReportedLost` and `LostBookRecovered` events.
- Add commands/application services for report and recovery.
- Update event handlers that transform Book state.
- Extend persistence mapping and state conversion.
- Update Patron Profile to remove/mark the checkout consistently.
- Update Daily Sheet to remove overdue tracking for the completed checkout.
- Add architecture/compiler-focused tests showing invalid transitions are unavailable or rejected.

## Acceptance criteria

- Checked-out book can transition to `LostBook`.
- Available or on-hold books cannot be reported lost through this flow.
- Lost book is excluded from available-book queries.
- Recovery transitions `LostBook` to `AvailableBook`.
- Duplicate report/recovery is handled predictably.
- Existing state transitions continue to compile and pass tests.

## Testing

- Checked-out -> lost.
- Lost -> available recovery.
- Available/on-hold -> lost rejection.
- Lost -> normal return rejection.
- Profile and Daily Sheet updates.
- Persistence round-trip for `LostBook`.
- Duplicate event handling if an event is delivered twice.

## Out of scope

- Charging replacement fees; handled in Issue 10.
- Inventory audits.
- Physical branch transfer.

## Definition of done

- `LostBook` is a first-class state, not only an enum branch hidden in services.
- All mappings and projections understand the state.
- The PR includes the updated lifecycle diagram.

---

# Issue 10 - Implement Book Fees with a `Money` Value Object and Fee Policy

**Type:** Feature  
**Difficulty:** Advanced  
**Suggested labels:** `learning/java`, `learning/ddd`, `domain-model`, `events`, `cqrs`  
**Depends on:** Issues 5 and 9

## Learning objectives

- Design a precise monetary value object.
- Separate Catalogue data from Lending decisions.
- Practice integration-event payload design.
- Model a policy with explicit domain language.

## Context

The repository domain description states that books may have retrieval or usage fees, but this behavior is not fully represented. Add a small, explicit fee model without building a complete payments system.

## Business rules

- `Money` contains amount and currency.
- Amount uses `BigDecimal`, never `double` or `float`.
- Amount must be zero or positive.
- Currency is required and represented by `java.util.Currency` or a validated ISO currency code.
- A book instance may have a retrieval fee and a usage fee.
- Catalogue owns the configured fees because they describe the catalogued copy.
- Lending receives the fee data through its integration boundary and uses it in hold/checkout decisions or views.
- A lost-book replacement charge is outside the core payment flow, but Issue 9's lost event should expose enough information for a fee assessment projection.
- No actual payment gateway is introduced.

## Implementation tasks

- Add `Money` as an immutable value object with arithmetic/equality behavior needed by the feature.
- Extend Catalogue's book-instance creation to accept optional fees.
- Validate fee input at the API/application boundary and in `Money`.
- Extend or version the `BookInstanceAddedToCatalogue` integration event to carry fee information.
- Update Lending's book creation handler and persistence model.
- Add a query representation showing fees for a book.
- Add a named `FeePolicy` or equivalent if fees influence hold/checkout eligibility.
- Document whether zero fee is represented as `Money.zero(currency)` or absence, and use one approach consistently.

## Acceptance criteria

- Monetary values are never represented with binary floating-point types.
- Invalid negative amounts and invalid currencies are rejected.
- Fee data crosses the Catalogue/Lending boundary through a defined contract.
- Existing book instances without fees remain supported.
- Fee information is visible on an appropriate read endpoint.
- The domain does not depend on JSON or database column types.

## Testing

- `Money` equality and scale behavior.
- Addition of same-currency values.
- Rejection of cross-currency arithmetic unless explicitly converted outside the value object.
- Negative amount rejection.
- Catalogue persistence and event publication.
- Lending event handling and persistence.
- Backward-compatible no-fee scenario.

## Out of scope

- Payment processing.
- Refunds.
- Exchange rates.
- Accounting journals.
- Fine collection.

## Definition of done

- Fees are modeled in business terms.
- The context ownership decision is documented.
- Tests prove precision, validation, integration mapping, and backward compatibility.

---

# Issue 11 - Build an Available Books Search Read Model

**Type:** Feature  
**Difficulty:** Intermediate to advanced  
**Suggested labels:** `learning/spring`, `learning/ddd`, `cqrs`, `infrastructure`  
**Depends on:** Issues 9 and 10

## Learning objectives

- Build a CQRS projection optimized for queries.
- Practice JDBC query design, pagination, and DTO mapping.
- Keep query concerns outside aggregates.
- Understand projection updates from domain/integration events.

## Context

The system has command-side Book states, but patrons need a way to discover books that can currently be held or checked out. Do not expose the command repository as a general search service. Create a dedicated read model.

## Requested API

```http
GET /books/available?branchId={uuid}&type=CIRCULATING&page=0&size=20
```

Optional filters:

- `branchId`;
- book type;
- ISBN;
- title/author when the Catalogue information is available in the projection;
- minimum/maximum fee from Issue 10.

## Query behavior

- Only books currently represented as `AvailableBook` are returned.
- `BookOnHold`, `CheckedOutBook`, and `LostBook` are excluded.
- Results are paginated and have deterministic ordering.
- Empty result sets return `200` with an empty collection.
- Invalid pagination/filter parameters return `400` using the shared error model.

## Implementation tasks

- Define a read-side interface such as `AvailableBooks`.
- Define an immutable query object such as `AvailableBooksQuery`.
- Create a projection table or a clearly isolated SQL read model.
- Update the projection from book lifecycle events:
  - instance added;
  - placed on hold;
  - hold canceled/expired;
  - checked out;
  - returned;
  - reported lost;
  - recovered.
- Add a controller and response DTOs.
- Add pagination metadata and HATEOAS links if consistent with the existing API style.
- Do not return command-side `Book` objects from the controller.

## Acceptance criteria

- Search returns only available copies.
- Every supported Book transition updates the projection correctly.
- Filtering and pagination work together.
- Ordering is stable across repeated calls.
- Query code does not load Patron or Book aggregates.
- Lost books never appear until recovered.

## Testing

- Empty search.
- Filter by branch and type.
- Pagination first/middle/last page.
- Available -> on hold removal.
- On hold -> available restoration.
- Checked out -> returned restoration.
- Lost -> recovered restoration.
- Fee filter when Issue 10 is complete.
- Projection integration tests using the configured database.

## Out of scope

- Full-text search engine.
- Elasticsearch.
- Ranking/recommendations.
- Cross-library federation.

## Definition of done

- Query model is independent from command aggregates.
- Event-to-projection mappings are tested.
- API examples and filter semantics are documented.

---

# Issue 12 - Define Lending Transaction Boundaries and Prove Rollback Behavior

**Type:** Refactor and test request  
**Difficulty:** Advanced  
**Suggested labels:** `learning/spring`, `infrastructure`, `events`, `testing`, `advanced`  
**Depends on:** Issue 11

## Learning objectives

- Understand Spring transaction proxies and transaction managers.
- Learn transaction propagation for synchronous event listeners.
- Distinguish local atomicity from cross-context consistency.
- Write failure-injection integration tests.

## Context

Lending commands persist aggregate changes and publish events that update Book state and read models. Before moving to reliable asynchronous delivery, establish and document what is atomic inside the Lending data source.

## Required investigation

Document the current transaction behavior for one representative command, preferably placing a hold:

```text
PlaceOnHoldCommand
  -> Patron decision
  -> Patron persistence
  -> event publication
  -> Book state update
  -> Daily Sheet update
  -> Patron Profile update
```

Determine which operations share the Lending transaction manager and which do not.

## Requested behavior

For the synchronous immediate-consistency configuration:

- A failure in a Lending event handler must not leave a partially applied Lending operation when all affected records use the same Lending data source.
- Catalogue and Lending must not be presented as one transaction; cross-context reliability is handled in Issues 13-16.
- Transaction annotations must be placed on public Spring-managed application/infrastructure boundaries, not private methods or domain objects.

## Implementation tasks

- Identify the correct transaction boundary for representative Lending commands.
- Use the named Lending transaction manager explicitly where multiple managers create ambiguity.
- Avoid self-invocation assumptions that bypass Spring proxies.
- Create a test-only failing event listener or repository adapter that throws after one earlier update.
- Prove all expected Lending writes roll back.
- Add a second test proving successful commands commit all expected Lending changes.
- Add documentation explaining why this is a local transaction only.

## Acceptance criteria

- Failure-injection test starts with a clean database and proves no partial Lending state remains.
- Success test proves Patron, Book, and projections are updated.
- The selected transaction manager is unambiguous.
- Domain model classes contain no transaction annotations.
- No distributed/XA transaction is introduced.
- Existing immediate/eventual consistency tests remain meaningful.

## Testing

- Handler failure before Book update.
- Handler failure after Book update but before a projection update.
- Repository failure during aggregate persistence.
- Successful command commit.
- Verify transaction behavior through database assertions, not only mocks.

## Out of scope

- Atomic transaction across Catalogue and Lending.
- Message broker.
- Outbox implementation.

## Definition of done

- Transaction boundaries are documented in an ADR or architecture note.
- Integration tests prove rollback and commit behavior.
- The PR explains Spring proxy requirements and the selected annotation location.

---

# Issue 13 - Implement a Transactional Outbox for Catalogue Integration Events

**Type:** Infrastructure enhancement  
**Difficulty:** Advanced  
**Suggested labels:** `learning/spring`, `events`, `infrastructure`, `advanced`  
**Depends on:** Issue 12

## Learning objectives

- Understand the transactional outbox pattern.
- Learn local transaction guarantees across business data and event records.
- Practice scheduled publishing and event serialization.
- Distinguish domain events from integration events.

## Context

When Catalogue adds a book instance, Lending must create its corresponding book representation. Because Catalogue and Lending use separate context boundaries/data sources, a direct synchronous call cannot provide safe cross-context atomicity.

## Requested behavior

Saving a new Catalogue book instance must atomically save an outbox record in the same Catalogue transaction.

```text
Catalogue transaction
  1. Insert BookInstance
  2. Insert BookInstanceAdded integration event into outbox
  3. Commit
```

A separate publisher then forwards unpublished events.

## Outbox record requirements

At minimum:

- `event_id`;
- `event_type`;
- `aggregate_id` or book instance ID;
- serialized payload;
- occurrence timestamp;
- creation timestamp;
- publication status/timestamp;
- attempt count;
- optional last error for later issues.

## Implementation tasks

- Introduce a Catalogue-specific outbox table and persistence adapter.
- Define an integration-event contract independent from Catalogue persistence entities.
- Save the business change and outbox event under the Catalogue transaction manager.
- Add a scheduled publisher or explicit publisher service.
- Publish only committed outbox rows.
- Mark a row as published only after successful handoff to the configured event bus.
- Use a stable event type/version strategy.
- Ensure payload serialization/deserialization is tested.
- Do not delete published rows immediately; retain them for audit during this learning project.

## Acceptance criteria

- If outbox insertion fails, the book instance is not committed.
- If business insertion fails, no outbox record is committed.
- If publication fails after commit, the outbox row remains unpublished.
- Retrying publication does not create another Catalogue book instance.
- Event ID and occurrence time remain stable across retries.
- Existing no-fee and fee-carrying event payloads are supported.

## Testing

- Successful atomic BookInstance + outbox commit.
- Forced outbox insert failure rolls back BookInstance.
- Forced publisher failure leaves pending row.
- Later publisher retry succeeds.
- Serialization compatibility test.
- Multiple pending rows publish in deterministic batches.

## Out of scope

- Kafka/RabbitMQ.
- Exactly-once delivery.
- Cross-database transactions.
- Automatic cleanup/archiving policy.

## Definition of done

- Catalogue changes and outbox records are locally atomic.
- Failure scenarios are demonstrated by integration tests.
- The PR includes a sequence diagram for commit, publish failure, and retry.

---

# Issue 14 - Add an Idempotent Lending Inbox for Catalogue Events

**Type:** Infrastructure enhancement  
**Difficulty:** Advanced  
**Suggested labels:** `learning/ddd`, `events`, `infrastructure`, `testing`, `advanced`  
**Depends on:** Issue 13

## Learning objectives

- Understand at-least-once delivery.
- Implement idempotent event consumers.
- Practice database uniqueness and local consumer transactions.
- Separate integration handling from aggregate logic.

## Context

The outbox publisher may deliver the same integration event more than once, especially if the publisher crashes after Lending processes the event but before Catalogue marks it published. Lending must safely handle duplicates.

## Requested behavior

Create a Lending inbox/processed-event record. Processing must occur in one Lending transaction:

```text
1. Check/claim event ID
2. Create or update the Lending book representation
3. Record event as processed
4. Commit
```

## Business and technical rules

- The same `event_id` can affect Lending at most once.
- Duplicate delivery returns success/no-op rather than causing duplicate data.
- A different event with the same book ID must still be validated according to its event type and version.
- A database uniqueness constraint is required; an application-only `exists` check is not sufficient under concurrency.
- If processing fails, the inbox marker and domain changes roll back together.

## Implementation tasks

- Add a Lending inbox table with a unique `event_id`.
- Add a consumer adapter for `BookInstanceAdded` integration events.
- Wrap inbox claim, Book creation, and processed status in the Lending transaction manager.
- Add a unique constraint for the relevant Lending book identifier as defense in depth.
- Handle concurrent duplicate deliveries predictably.
- Keep event-deserialization DTOs outside the domain model.
- Add clear logs containing event ID and event type without sensitive payload dumping.

## Acceptance criteria

- First delivery creates the Lending book.
- Second delivery of the same event does not create a duplicate or fail the workflow.
- Concurrent duplicate deliveries result in one committed effect.
- A processing exception leaves no processed marker.
- Retrying after a transient failure can succeed.
- Handler behavior is covered by database-backed tests.

## Testing

- Sequential duplicate delivery.
- Concurrent duplicate delivery.
- Failure after inbox claim but before Book save.
- Failure after Book save but before processed marker.
- Retry after failure.
- Unknown event version rejection without partial state.

## Out of scope

- A general-purpose enterprise messaging framework.
- Global deduplication across all bounded contexts.
- Permanent dead-letter policy; handled in Issue 15.

## Definition of done

- Delivery is safely at-least-once.
- Idempotency is enforced by both transaction design and database constraints.
- The PR explains why exactly-once delivery is not assumed.

---

# Issue 15 - Add Retry, Dead-Letter Status, and Manual Event Replay

**Type:** Operational enhancement  
**Difficulty:** Advanced  
**Suggested labels:** `learning/spring`, `events`, `infrastructure`, `advanced`  
**Depends on:** Issues 13 and 14

## Learning objectives

- Build a resilient scheduled retry workflow.
- Distinguish transient and permanent failures.
- Implement safe manual recovery.
- Learn operational API/CLI design and auditability.

## Context

Outbox rows can remain unpublished or repeatedly fail in Lending. Operators need visibility and a safe way to retry without manually editing database rows.

## Requested behavior

Enhance event delivery with statuses such as:

```text
PENDING
PROCESSING
PUBLISHED
FAILED
DEAD_LETTER
```

Exact names may differ, but transitions must be explicit and tested.

## Retry rules

- Retry transient failures with bounded exponential backoff.
- Increment attempt count on each failed attempt.
- Store a sanitized last-error summary.
- Move an event to dead-letter after a configurable maximum attempt count.
- Never create a new event ID during retry or replay.
- Manual replay resets a dead-letter event to a retryable state only after validation.

## Administrative interface

Provide one safe option:

- a Spring Boot CLI command;
- an actuator-style custom endpoint; or
- a dedicated admin endpoint enabled only under an explicit admin/dev profile.

Required operations:

- list pending/failed/dead-letter events;
- inspect metadata without exposing unsafe stack traces;
- replay one event by ID;
- replay an explicitly filtered batch with a maximum limit.

## Implementation tasks

- Extend outbox schema and repository.
- Add configurable retry properties using Spring configuration properties.
- Implement backoff scheduling without sleeping worker threads.
- Prevent two publishers from claiming the same event concurrently.
- Add structured logs for status transitions.
- Add manual replay service and interface.
- Record who/what initiated manual replay when the chosen interface provides identity/context; otherwise record `manual` and timestamp.

## Acceptance criteria

- Transient failure is retried and can later publish successfully.
- Repeated permanent failure reaches dead-letter state.
- Dead-letter events are excluded from automatic retry.
- Manual replay reuses the original event ID and payload.
- Lending inbox makes replay safe when the event was already processed.
- Concurrent publishers do not publish one claimed event simultaneously.

## Testing

- Backoff sequence with a controllable clock/scheduler.
- Success after one and multiple transient failures.
- Dead-letter transition at configured maximum.
- Manual replay of an unprocessed event.
- Manual replay of an already processed event.
- Concurrent claim test.
- Sanitization of stored error messages.

## Out of scope

- User-facing admin UI.
- Distributed tracing platform.
- Broker-specific retry topics.

## Definition of done

- Failure handling no longer requires direct SQL changes.
- Replay is auditable and idempotent.
- Configuration and operational usage are documented.

---

# Issue 16 - Add Catalogue-to-Lending Consistency Reconciliation

**Type:** Operational and data-integrity enhancement  
**Difficulty:** Advanced  
**Suggested labels:** `learning/spring`, `events`, `infrastructure`, `testing`  
**Depends on:** Issues 14 and 15

## Learning objectives

- Understand eventual consistency repair.
- Build a reconciliation process without coupling domain models directly.
- Practice batch processing and safe repair commands.
- Learn the difference between detection and correction.

## Context

Even with an outbox and inbox, historic defects, manual data changes, or unexpected failures can leave Catalogue and Lending inconsistent. Add a reconciliation process that detects and optionally repairs missing Lending representations.

## Reconciliation rules

Detect at least:

- Catalogue book instance exists, but Lending has no corresponding Book.
- Lending Book exists, but no Catalogue instance can be found.
- Catalogue and Lending disagree about immutable integration attributes such as book type or configured fees.
- A published Catalogue event has no Lending inbox record after a configurable age.

## Requested modes

1. **Report-only mode** - default; makes no data changes.
2. **Repair-missing mode** - republishes the original or reconstructed integration event safely.
3. **Strict mode** - fails the job when any mismatch is found, useful for CI/testing.

## Implementation tasks

- Define a reconciliation report DTO independent from both domain models.
- Query each context through explicit ports/read services rather than sharing repository implementation classes.
- Add batch size and maximum-record configuration.
- Add a CLI command or profile-restricted admin endpoint.
- Prefer replaying an integration event over direct SQL insertion.
- Ensure repairs go through the Lending inbox/idempotent handler.
- Record reconciliation run metadata and outcome counts.
- Do not automatically delete orphaned Lending records; report them for manual decision.

## Acceptance criteria

- Report-only mode never mutates data.
- Missing Lending Book is detected.
- Repair mode creates the missing Lending Book through normal event handling.
- Re-running repair is safe and produces no duplicates.
- Orphaned Lending records are reported but not deleted.
- Mismatched attributes are clearly identified.
- Large sets are processed in bounded batches.

## Testing

- Fully consistent data.
- Missing Lending Book.
- Orphaned Lending Book.
- Mismatched book type/fee.
- Already processed event replay.
- Batch boundary behavior.
- Report-only no-write assertion.

## Out of scope

- Automatic deletion.
- Arbitrary two-way data synchronization.
- Fixing unrelated Patron projections.

## Definition of done

- Operators can detect and repair the primary cross-context inconsistency without editing SQL.
- The PR documents why reconciliation remains valuable even after implementing outbox/inbox.

---

# Issue 17 - Add Patron Profile and Daily Sheet Projection Rebuild

**Type:** CQRS infrastructure enhancement  
**Difficulty:** Advanced  
**Suggested labels:** `learning/ddd`, `cqrs`, `events`, `infrastructure`, `advanced`  
**Depends on:** Issues 11, 14, and 15

## Learning objectives

- Understand projection rebuild strategies.
- Learn the limits of current-state persistence versus event sourcing.
- Build replaceable read models.
- Practice versioned projection migrations.

## Context

Patron Profile and Daily Sheet are read models updated from events. A bug in a projector can leave them incorrect. Add a controlled way to rebuild projections from an authoritative source.

## Required design decision

The project is not fully event sourced. Therefore, first document which authoritative data can rebuild each projection:

- retained domain/integration event history, when complete enough;
- command-side current-state tables;
- or a combination.

Do not claim an exact historical replay when only current state is available.

## Requested behavior

Provide projection rebuild operations for:

- Patron Profile;
- Daily Sheet;
- Available Books projection from Issue 11.

Each projector must have a projection name and version.

## Implementation tasks

- Define a `ProjectionRebuilder` application/infrastructure contract.
- Add projector version metadata.
- Build new projection data in temporary/versioned tables where practical.
- Swap or activate the rebuilt version only after successful completion.
- Keep the previous projection available until activation succeeds.
- Add report/dry-run mode showing expected row counts.
- Prevent normal projection handlers from corrupting the rebuild; choose pause, dual-write, or catch-up strategy and document it.
- Add an admin/CLI trigger with bounded scope, such as one patron or all patrons.

## Acceptance criteria

- A corrupted/missing Patron Profile can be rebuilt.
- Daily Sheet due dates match authoritative hold/checkout state.
- Available Books projection matches command-side Book states.
- Failed rebuild does not destroy the active projection.
- Rebuild is repeatable.
- Projection version is visible in diagnostics.
- Documentation clearly states whether historical event replay or current-state reconstruction is used.

## Testing

- Delete/corrupt projection rows, then rebuild.
- Rebuild one patron only.
- Full rebuild.
- Failure before activation.
- Events arriving during rebuild according to the chosen strategy.
- Row-count and semantic consistency assertions.

## Out of scope

- Converting the entire application to event sourcing.
- Rebuilding Catalogue command data.
- Zero-downtime guarantees across multiple deployed nodes unless explicitly implemented.

## Definition of done

- Read models are recoverable without direct handcrafted SQL.
- Rebuild semantics and limitations are documented honestly.
- Tests prove active projections survive a failed rebuild.

---

# Issue 18 - Add Domain Event, Outbox, and Projection Observability

**Type:** Operational enhancement  
**Difficulty:** Intermediate to advanced  
**Suggested labels:** `learning/spring`, `events`, `infrastructure`, `testing`  
**Depends on:** Issues 15-17

## Learning objectives

- Use Spring Boot Actuator and Micrometer.
- Design useful metrics rather than logging everything.
- Measure eventual-consistency health.
- Practice structured, correlation-friendly logging.

## Context

The project already includes Actuator and Prometheus support. Add metrics that help explain whether events and projections are healthy.

## Required metrics

At minimum expose:

- events published, grouped by event type and outcome;
- event handler duration;
- event handler failures;
- pending outbox count;
- oldest pending outbox age;
- dead-letter count;
- inbox duplicate count;
- reconciliation mismatches;
- projection rebuild duration/outcome;
- projection lag where a reliable timestamp is available.

## Metric design constraints

- Avoid high-cardinality labels such as event ID, patron ID, book ID, email, or exception message.
- Event type labels must come from a bounded known set.
- IDs may appear in structured logs but not metric tags.
- Error logs must not include sensitive request payloads.
- Metrics code must stay outside the domain model.

## Implementation tasks

- Add a small observability adapter around event publication/handling.
- Use Micrometer timers, counters, and gauges appropriately.
- Add health indicators for severe conditions such as non-zero dead-letter count or excessive oldest-pending age.
- Add structured log fields for `eventId`, `eventType`, `attempt`, and context.
- Document Prometheus queries or actuator endpoints for common checks.
- Add tests with a simple meter registry.

## Acceptance criteria

- Successful and failed event handling changes the correct counters.
- Pending/dead-letter gauges match database state.
- Metrics contain no unbounded identifier tags.
- Health details identify the failing subsystem without exposing stack traces.
- Existing `@Timed` behavior remains valid or is deliberately replaced.
- Documentation includes at least five useful diagnostic queries/checks.

## Testing

- Counter increment on success/failure.
- Timer recording.
- Duplicate inbox metric.
- Dead-letter health state.
- Pending-age calculation using fixed clock.
- Assert forbidden high-cardinality tags are absent.

## Out of scope

- Grafana dashboard provisioning, unless added as an optional small extra.
- External APM vendor integration.
- Distributed tracing across microservices.

## Definition of done

- An operator can detect stuck events and projection problems from metrics.
- The PR explains metric cardinality and why identifiers are excluded from tags.

---

# Issue 19 - Strengthen Bounded-Context and Layer Boundaries with ArchUnit

**Type:** Architecture test enhancement  
**Difficulty:** Intermediate to advanced  
**Suggested labels:** `learning/java`, `learning/ddd`, `architecture`, `testing`  
**Depends on:** All feature issues, especially Issues 11-18

## Learning objectives

- Convert architectural intent into executable rules.
- Understand package dependency direction.
- Detect accidental bounded-context coupling.
- Practice ArchUnit beyond a single framework-dependency rule.

## Context

The repository already demonstrates ArchUnit rules that keep domain models independent from infrastructure and Spring. Expand this safety net to cover the architecture introduced by the learning backlog.

## Required rules

Add rules for at least the following:

1. Lending model packages do not depend on Lending infrastructure or web packages.
2. Catalogue domain/application code does not depend on Lending implementation packages.
3. Lending does not import Catalogue persistence/database classes.
4. Cross-context communication uses explicitly allowed integration-event contracts or ports.
5. Web/controller classes do not access JDBC/DataSource directly.
6. Domain model classes do not use Spring annotations.
7. Controllers do not construct aggregate roots directly.
8. Projection/read-model packages do not mutate command aggregates.
9. Outbox/inbox infrastructure is not imported by domain classes.
10. Public context APIs are limited to explicitly approved packages/types.

## Implementation tasks

- Inspect existing ArchUnit test organization and extend it rather than creating unrelated duplicate suites.
- Define reusable package constants/predicates.
- Add one deliberately violating test fixture or temporary proof during development, then remove it before completion.
- Produce readable failure messages that explain the architectural rule.
- Document exceptions explicitly; do not use broad ignore patterns.
- Add a short architecture dependency diagram matching the rules.

## Acceptance criteria

- All required rules execute in the normal test build.
- A representative forbidden dependency is detected during test development.
- Allowed integration-event dependencies still pass.
- Rules do not depend on developer machine paths.
- No broad `ignoreDependency` suppresses an entire context boundary.
- Architecture docs and executable rules agree.

## Testing

- Run the full architecture test suite.
- Demonstrate one sample failure in the PR description or commit history without leaving broken code.
- Verify new outbox, inbox, metrics, and projections comply.

## Out of scope

- Rewriting all package names.
- Splitting Maven modules; handled in Issue 20.
- Static analysis tools unrelated to architecture boundaries.

## Definition of done

- Architectural decisions are enforced automatically.
- New contributors receive actionable test failures when crossing boundaries.
- The PR explains each rule in DDD/hexagonal terms.

---

# Issue 20 - Split the Modular Monolith into Maven Modules Without Creating Microservices

**Type:** Architecture refactor / capstone  
**Difficulty:** Advanced capstone  
**Suggested labels:** `learning/java`, `learning/spring`, `learning/ddd`, `architecture`, `advanced`  
**Depends on:** Issue 19 and completion of the earlier backlog

## Learning objectives

- Practice Maven multi-module design.
- Reinforce bounded-context compile-time boundaries.
- Understand the difference between a module and a microservice.
- Wire multiple Spring configurations in one deployable application.
- Manage test scope and shared contracts carefully.

## Context

The repository is a modular monolith organized primarily through packages. Convert the fork into a Maven multi-module build while keeping one deployable application and preserving behavior.

## Target module direction

A reasonable starting point is:

```text
library-parent
├── library-commons
├── library-catalogue
├── library-lending
├── library-integration-contracts
└── library-application
```

The exact names may differ, but module responsibilities must be explicit.

## Dependency rules

- `library-application` assembles/runs the Spring application.
- `library-catalogue` and `library-lending` must not depend on each other's implementation modules.
- Shared integration-event contracts may live in a small dedicated module.
- `library-commons` must remain small and contain genuinely shared technical/domain-neutral concepts only.
- Do not move business concepts to commons merely to solve a dependency-cycle problem.
- The final application remains one process and one deployment unit.

## Implementation tasks

- Convert the root `pom.xml` to a parent/aggregator.
- Create child module POMs with minimal dependencies.
- Move source and resource files without changing package names unnecessarily.
- Move tests to the module whose behavior they verify.
- Keep cross-context integration tests in the application/integration-test module.
- Update Spring application assembly and context imports.
- Preserve separate Catalogue and Lending transaction managers/data sources.
- Make ArchUnit rules aware of the module structure or supplement them with Maven dependency enforcement.
- Update Docker/build/CI configuration.
- Document the dependency graph and why the result is still a modular monolith.

## Acceptance criteria

- One Maven command builds all modules and runs all tests.
- The application starts and existing endpoints behave unchanged.
- Catalogue and Lending compile independently against only allowed contracts.
- No circular Maven dependencies exist.
- Integration events cross through the contracts module or another explicitly documented boundary.
- Outbox/inbox, reconciliation, rebuild, and metrics features still work.
- Container/build configuration produces one runnable application artifact.

## Testing

- Full unit, integration, and architecture test suite.
- Application startup test.
- Patron registration and profile API smoke test.
- Hold, checkout, renewal, lost/recovery flow.
- Catalogue event -> outbox -> Lending inbox flow.
- Reconciliation and projection rebuild smoke tests.
- Maven dependency-tree review for forbidden coupling.

## Out of scope

- Deploying Catalogue and Lending independently.
- Network calls between contexts.
- Kubernetes/service discovery.
- Replacing all databases or frameworks.

## Definition of done

- The project is a clean Maven multi-module modular monolith.
- Compile-time boundaries reinforce the DDD context map.
- The PR includes a before/after dependency diagram and a brief explanation of why bounded context does not automatically mean microservice.

---

# Recommended Pull Request Template for Every Issue

```markdown
## Business behavior

Describe the behavior added or changed in domain language.

## DDD decisions

- Aggregate/value object/policy/event/read model involved:
- Why the behavior belongs there:
- Bounded-context impact:

## Spring/Java learning

- Spring concepts practiced:
- Java concepts practiced:

## Implementation summary

- Files/packages changed:
- Database/schema changes:
- API changes:

## Tests

- Commands run:
- New scenarios covered:
- Result:

## Trade-offs and follow-up work

Explain one design trade-off and list any intentionally deferred work.
```

# Final Learning Outcome

After completing all 20 issues, you should be able to explain and demonstrate:

- Spring MVC controllers, validation, exception handling, configuration, transactions, scheduling, Actuator, and Micrometer;
- Java immutability, value objects, `BigDecimal`, `Clock`, type-state modeling, equality, and modular builds;
- DDD aggregates, invariants, policies, repositories, domain events, integration events, bounded contexts, ubiquitous language, and context ownership;
- CQRS read models, projection updates, projection rebuilds, eventual consistency, outbox/inbox, idempotency, retries, reconciliation, and operational recovery;
- executable architecture rules and the difference between a modular monolith and microservices.
