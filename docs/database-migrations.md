# Database Migrations

Database schema changes are managed with Flyway. Application startup no longer executes the legacy `create_*.sql` scripts directly.

## Migration streams

The application has two independent H2 databases and therefore two migration locations:

- Catalogue: `src/main/resources/db/migration/catalogue`
- Lending: `src/main/resources/db/migration/lending`

Each database has its own `flyway_schema_history` table.

Current migrations:

| Context | Version | Purpose |
|---|---:|---|
| Catalogue | V1 | Initial catalogue schema |
| Lending | V1 | Initial lending schema before patron suspension and hold extension |
| Lending | V2 | Patron status and suspension reason |
| Lending | V3 | Nullable hold expiry and hold extension count |

## Naming and versioning

Use Flyway's versioned migration naming convention:

```text
V<integer>__<short_description>.sql
```

Examples:

```text
V4__add_checkout_renewal.sql
V5__add_lost_book_state.sql
```

Rules:

1. Never modify a migration after it has been applied or merged.
2. Add a new migration for every schema change, including constraints and indexes.
3. Keep Catalogue and Lending migrations in their own streams.
4. Do not use `IF NOT EXISTS` to hide migration ordering or drift problems.
5. Run `./mvnw clean verify` after adding or changing a migration.

Flyway validates applied migration checksums on migration/startup. A changed committed migration therefore fails validation instead of silently changing schema history.

## Local startup and tests

Each database configuration creates its datasource first and then runs its Flyway migration bean before JDBC operations and transaction infrastructure are made available.

A new in-memory database is therefore created entirely from Flyway migrations on every application/test startup.

Migration integration tests also verify:

- an empty Catalogue database reaches the latest schema;
- a Lending V1 database upgrades through V2 and V3;
- UUID and timestamp values survive the upgrade;
- patron status defaults to `ACTIVE`;
- hold extension count defaults to `0`;
- `hold_database_entity.till` becomes nullable;
- Flyway metadata contains the expected successful versions;
- committed migration checksums validate successfully.

## Adopting an existing legacy developer database

Do not enable `baselineOnMigrate` globally. Automatic baselining can make an accidentally selected non-empty database look valid.

For a persisted developer database that was originally created by the old `create_*.sql` scripts:

1. Back up the database.
2. Identify which historical schema it matches.
3. Explicitly baseline that database at the matching version once.
4. Run normal Flyway migration to latest.
5. Verify `flyway_schema_history` before continuing development.

For Lending, use these baseline versions:

| Existing schema | Baseline version |
|---|---:|
| No `patron_database_entity.status` and no `hold_database_entity.extension_count` | 1 |
| Has patron `status` / `suspension_reason`, but no hold `extension_count` | 2 |
| Has patron status fields and hold `extension_count` with nullable `till` | 3 |

The Catalogue legacy schema corresponds to Catalogue version 1.

After baselining, do not rerun the old creation scripts. Future schema changes must be represented only by new Flyway migrations.

## Failure handling

Flyway migration is part of database initialization. If migration or checksum validation fails, the dependent JDBC infrastructure is not initialized and application startup fails. Do not catch or suppress migration failures.
