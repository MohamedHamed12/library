# Library DDD Learning Lab

This repository is my personal fork of the original
[`ddd-by-examples/library`](https://github.com/ddd-by-examples/library) project.

I use this fork as a hands-on learning and improvement project for Java, Spring
Boot, Domain-Driven Design, Hexagonal Architecture, modular monoliths, CQRS,
domain events, transaction boundaries, automated testing, and ArchUnit.

The original project already contains a strong DDD example. My goal is not to
replace its design, but to study it, extend it carefully, and practice making
production-oriented improvements without breaking the domain model.

> This is an educational fork. Credit for the original application, domain
> discovery, and architecture belongs to the original maintainers.

---

## Why This Fork Exists

I created this fork to move from reading about DDD and Spring architecture to
implementing the concepts in a real codebase.

The work is organized as a **20-issue learning roadmap**. The roadmap was
designed with help from Claude and is implemented incrementally through small,
reviewable changes.

Each issue focuses on one or more engineering skills:

- understanding an existing domain model;
- extending behavior without weakening invariants;
- separating domain, application, web, and infrastructure concerns;
- improving API design and error handling;
- testing business rules and technical failure paths;
- introducing reliable event-driven integration patterns;
- strengthening modular boundaries;
- evolving a modular monolith without prematurely creating microservices.

AI is used as a planning and review assistant. Every change is still inspected,
implemented, tested, and validated in the repository.

---

## Domain Overview

The system models a public library.

Patrons can:

- place available books on hold;
- cancel holds;
- check out books;
- return books;
- view current holds and checkouts;
- interact with circulating and restricted books under different policies.

Important domain rules include:

- one available book instance can be held by only one patron at a time;
- regular patrons can have at most five active holds;
- researcher patrons can hold restricted books;
- only researcher patrons can create open-ended holds;
- patrons with too many overdue checkouts can be blocked from placing a hold;
- closed-ended holds expire after their configured duration;
- checked-out books can become overdue.

The system is split into two main bounded contexts:

- **Catalogue** — manages books and book instances;
- **Lending** — manages patrons, books, holds, checkouts, daily sheets, and
  patron profiles.

---

## Architecture

The project is a **modular monolith** organized primarily by bounded context and
business capability.

```text
library
├── catalogue
├── commons
│   ├── aggregates
│   ├── commands
│   └── events
└── lending
    ├── book
    │   ├── application
    │   ├── infrastructure
    │   └── model
    ├── dailysheet
    │   ├── infrastructure
    │   └── model
    ├── librarybranch
    │   └── model
    ├── patron
    │   ├── application
    │   ├── infrastructure
    │   └── model
    └── patronprofile
        ├── infrastructure
        ├── model
        └── web
```

![Component diagram](docs/c4/component-diagram.png)

### Main Architectural Principles

#### Domain-first design

Business rules belong in the domain model. The domain should not depend on
Spring MVC, HTTP, database implementation details, or controller classes.

#### Hexagonal Architecture

The lending context separates domain logic, application use cases, ports,
infrastructure adapters, and REST delivery.

#### Explicit state transitions

Book states are represented using types such as `AvailableBook`, `BookOnHold`,
and `CheckedOutBook`. This makes illegal operations harder to express and moves
some validation from runtime checks to the type system.

#### CQRS

Write models and read models are separated where their responsibilities differ.
Patron Profiles and Daily Sheets are examples of dedicated read models.

#### Domain events

Aggregates communicate through events. The roadmap gradually improves event
reliability by adding transaction boundaries, an outbox, inbox/idempotency,
retry handling, replay, reconciliation, and observability.

#### Architecture tests

ArchUnit is used to protect dependency rules and prevent framework or
infrastructure concerns from leaking into the domain.

---

## Technology Stack

- Java 11
- Spring Boot 2.2
- Spring MVC
- Spring Data JDBC
- JDBC Template
- Maven
- H2
- Vavr
- Lombok
- Groovy
- Spock
- JUnit
- ArchUnit
- Micrometer
- Prometheus
- Grafana
- Docker

The project intentionally does not use JPA/Hibernate. Persistence is implemented
with Spring Data JDBC, `JdbcTemplate`, and explicit SQL where appropriate.

---

## 20-Issue Learning Roadmap

The roadmap progresses from API and application-layer improvements to advanced
event reliability and modular architecture.

### Phase 1 — API and Application Foundations

1. **Add a Combined Patron Profile Summary Endpoint**
2. **Introduce Request Validation and a Consistent REST Error Model**
3. **Introduce a Clock Port and Remove Direct Calls to `Instant.now()`**
4. **Add a Patron Registration Use Case and REST Endpoint**

### Phase 2 — Domain Modeling

5. **Model Patron Email as an Immutable `EmailAddress` Value Object**
6. **Add Patron Suspension and Reactivation**
7. **Add a Hold Extension Use Case**
8. **Add Checkout Renewal with a Domain Policy**
9. **Introduce a `LostBook` State and Reporting Flow**
10. **Implement Book Fees with a `Money` Value Object and Fee Policy**

### Phase 3 — CQRS and Transaction Boundaries

11. **Build an Available Books Search Read Model**
12. **Define Lending Transaction Boundaries and Prove Rollback Behavior**

### Phase 4 — Reliable Event-Driven Integration

13. **Implement a Transactional Outbox for Catalogue Integration Events**
14. **Add an Idempotent Lending Inbox for Catalogue Events**
15. **Add Retry, Dead-Letter Status, and Manual Event Replay**
16. **Add Catalogue-to-Lending Consistency Reconciliation**
17. **Add Patron Profile and Daily Sheet Projection Rebuild**
18. **Add Domain Event, Outbox, and Projection Observability**

### Phase 5 — Architecture Enforcement and Modularization

19. **Strengthen Bounded-Context and Layer Boundaries with ArchUnit**
20. **Split the Modular Monolith into Maven Modules Without Creating
    Microservices**

Progress, acceptance criteria, dependencies, and implementation notes are
tracked in the repository's GitHub Issues.

---

## Engineering Rules for This Fork

1. Preserve the language and invariants of the domain.
2. Prefer small, reviewable changes over large rewrites.
3. Keep HTTP and Spring MVC concepts at the web boundary.
4. Keep the domain independent from infrastructure frameworks.
5. Represent expected business rejection separately from technical failure.
6. Add tests before or together with behavior changes.
7. Test success, rejection, not-found, validation, and unexpected failure paths.
8. Protect architecture decisions with automated tests where possible.
9. Document important mappings and trade-offs.
10. Do not introduce microservices only for the sake of using microservices.

---

## Getting Started

### Requirements

- Java 11
- Maven
- Docker and Docker Compose, optional

### Clone

```bash
git clone https://github.com/MohamedHamed12/library.git
cd library
```

### Run Unit Tests

```bash
mvn test
```

### Run the Full Verification Build

```bash
mvn clean verify
```

Use `verify` when validating changes that include integration tests.

### Run the Application

```bash
mvn spring-boot:run
```

The application starts at:

```text
http://localhost:8080
```

### Build the JAR

```bash
mvn clean package
```

The generated JAR is placed under `target/`.

---

## Docker

Build after creating the JAR:

```bash
docker build -t spring/library .
```

Or use the multi-stage build:

```bash
docker build -t spring/library -f Dockerfile.build .
```

Run the container:

```bash
docker run --rm --name spring-library -p 8080:8080 spring/library
```

---

## Metrics and Monitoring

Run the application with Prometheus and Grafana:

```bash
docker-compose up
```

Default services:

| Service | Address |
|---|---|
| Application metrics | `http://localhost:8080/actuator/prometheus` |
| Prometheus | `http://localhost:9090` |
| Grafana | `http://localhost:3000` |

The observability roadmap will later expand this area to include domain events,
outbox processing, retries, dead-letter state, projections, and reconciliation.

---

## Testing Strategy

The project uses multiple levels of testing:

- domain unit tests for business rules and policies;
- application tests for use-case orchestration;
- web integration tests for HTTP contracts;
- persistence tests for database mappings;
- event-flow tests;
- architecture tests with ArchUnit;
- rollback and consistency tests for transaction behavior.

Tests are written in a BDD-oriented style and use domain language wherever
possible.

---

## Original Project Documentation

The original repository contains detailed material about the discovery and
design process:

- [Big Picture EventStorming](docs/big-picture.md)
- [Example Mapping](docs/example-mapping.md)
- [Design-Level EventStorming](docs/design-level.md)
- [Architecture diagrams](docs/c4/)
- [Domain discovery images](docs/images/)

These documents remain valuable references while extending the fork.

---

## Contribution Workflow

This repository is primarily a personal learning project, but constructive
feedback and pull requests are welcome.

For a change:

1. Select or create an issue.
2. Confirm its acceptance criteria.
3. Create a focused branch.
4. Add or update tests.
5. Implement the smallest correct change.
6. Run `mvn clean verify`.
7. Confirm architecture tests still pass.
8. Document meaningful architectural decisions.
9. Open a pull request linked to the issue.

Suggested branch naming:

```text
issue-<number>-<short-description>
```

Example:

```text
issue-2-rest-error-model
```

---

## Credits

This repository is based on the excellent
[`ddd-by-examples/library`](https://github.com/ddd-by-examples/library)
project.

The original maintainers created the initial domain model, EventStorming
material, architecture, and implementation. This fork keeps that work visible
and builds an additional learning roadmap on top of it.

The 20-issue enhancement roadmap was prepared with AI assistance from Claude.
Implementation decisions, code review, testing, and final responsibility remain
with the repository owner.

---

## References

1. Eric Evans — *Domain-Driven Design: Tackling Complexity in the Heart of
   Software*
2. Vaughn Vernon — *Implementing Domain-Driven Design*
3. Alberto Brandolini — *Introducing EventStorming*
4. Scott Wlaschin — *Domain Modeling Made Functional*
5. Robert C. Martin — *Clean Architecture*
6. Simon Brown — *Software Architecture for Developers*
7. Martin Fowler — *Patterns of Enterprise Application Architecture*