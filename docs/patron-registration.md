# Patron Registration Flow

```mermaid
sequenceDiagram
    actor Client
    participant Controller as PatronController
    participant Service as RegisteringPatron
    participant Repo as Patrons
    participant Events as DomainEvents

    Client->>Controller: POST /patrons
    Controller->>Service: RegisterPatronCommand(timestamp, type)
    Service->>Repo: publish(PatronCreated)
    Repo->>Events: publish(normalized event)
    Repo-->>Service: Patron
    Service-->>Controller: PatronId
    Controller-->>Client: 201 Created + Location /profiles/{patronId}
```

The controller owns HTTP concerns, the application service generates the identifier and timestamped registration command, and the repository persists the new aggregate by handling `PatronCreated`.