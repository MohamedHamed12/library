# Database integration testing

PostgreSQL is the reference database for database-backed integration tests. Tests start an ephemeral PostgreSQL 18 container with Testcontainers and use Spring Boot service connections to expose its JDBC connection details to the application database configuration.

## H2 decision

H2 remains the lightweight embedded fallback used by the application when no external JDBC connection details are available. It is not the compatibility oracle for database integration tests.

Unit tests do not import the Testcontainers configuration and remain independent from Docker. Database integration tests run against PostgreSQL.

Moving the integration suite to PostgreSQL exposed H2-specific persistence SQL such as `NEXT VALUE FOR`, qualified target columns in `UPDATE` statements, and direct `Instant` JDBC binding. Those paths now rely on migrated identity columns, portable update assignments, and explicit timestamp conversion so the same persistence code works with the H2 fallback and PostgreSQL reference database.

## Test lifecycle

`PostgreSQLTestConfiguration` owns the reusable PostgreSQL container definition. Catalogue and Lending integration contexts import that configuration, and the regular database configuration consumes the resulting `JdbcConnectionDetails`.

Flyway still owns schema creation. Each Spring integration context runs its normal Catalogue or Lending migration stream against PostgreSQL before database access begins.

A test execution listener truncates PostgreSQL application tables after every Spring test method while preserving `flyway_schema_history`. This prevents test order dependencies while allowing the Spring application context and container to be reused efficiently.

`FlywayMigrationIT` uses unique PostgreSQL schemas for its clean-install and upgrade scenarios so the two migration histories cannot interfere with one another.

## Running locally

Docker must be available to run the database integration suite.

```bash
./mvnw verify
```

Running unit tests alone does not start PostgreSQL containers:

```bash
./mvnw test
```

## CI

CircleCI enables its remote Docker environment before `./mvnw verify`. Testcontainers therefore uses the same PostgreSQL-backed integration strategy in CI as it does locally; no separately provisioned PostgreSQL service is required.

## Coverage

The PostgreSQL-backed suite exercises:

- Catalogue and Lending persistence paths
- Flyway clean installation and version upgrades
- UUID persistence and lookup
- nullable/open-ended hold timestamps
- optimistic locking through the existing repository integration test
- existing unique constraints
- transaction rollback behavior

The current schema does not define application `CHECK` or foreign-key constraints. Those should be tested against PostgreSQL when they are introduced through future Flyway migrations rather than inventing new schema rules solely for test infrastructure.
