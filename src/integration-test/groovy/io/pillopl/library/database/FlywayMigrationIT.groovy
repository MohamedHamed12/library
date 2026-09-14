package io.pillopl.library.database

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import spock.lang.Specification

import javax.sql.DataSource
import java.sql.Timestamp
import java.time.Instant

class FlywayMigrationIT extends Specification {

    def 'creates catalogue schema from an empty database'() {
        given:
            def database = database()
            def flyway = flyway(database, 'classpath:db/migration/catalogue')
            def jdbc = new JdbcTemplate(database)

        when:
            flyway.migrate()
            jdbc.update(
                    'INSERT INTO catalogue_book_instance (isbn, book_id) VALUES (?, ?)',
                    '9780134685991',
                    UUID.randomUUID())

        then:
            jdbc.queryForObject('SELECT COUNT(*) FROM catalogue_book_instance', Integer) == 1
            jdbc.queryForObject('SELECT COUNT(*) FROM flyway_schema_history WHERE success = TRUE', Integer) == 1
            noExceptionThrown()

        when:
            flyway.validate()

        then:
            noExceptionThrown()

        cleanup:
            database.shutdown()
    }

    def 'upgrades lending version one schema to the latest version'() {
        given:
            def database = database()
            def jdbc = new JdbcTemplate(database)
            def versionOne = Flyway.configure()
                    .dataSource(database)
                    .locations('classpath:db/migration/lending')
                    .target(MigrationVersion.fromVersion('1'))
                    .load()
            def latest = flyway(database, 'classpath:db/migration/lending')
            def patronId = UUID.randomUUID()
            def holdTill = Timestamp.from(Instant.parse('2026-09-14T12:00:00Z'))

        when:
            versionOne.migrate()
            jdbc.update(
                    'INSERT INTO patron_database_entity (patron_type, patron_id, email_address) VALUES (?, ?, ?)',
                    'Regular',
                    patronId,
                    'legacy@example.test')
            jdbc.update(
                    'INSERT INTO hold_database_entity (book_id, patron_id, library_branch_id, patron_database_entity, till) VALUES (?, ?, ?, ?, ?)',
                    UUID.randomUUID(),
                    patronId,
                    UUID.randomUUID(),
                    1,
                    holdTill)
            latest.migrate()

        then:
            jdbc.queryForObject('SELECT status FROM patron_database_entity WHERE patron_id = ?', String, patronId) == 'ACTIVE'
            jdbc.queryForObject('SELECT extension_count FROM hold_database_entity WHERE patron_id = ?', Integer, patronId) == 0
            jdbc.queryForObject('SELECT COUNT(*) FROM flyway_schema_history WHERE success = TRUE', Integer) == 3

        when:
            jdbc.update(
                    'INSERT INTO hold_database_entity (book_id, patron_id, library_branch_id, patron_database_entity, till) VALUES (?, ?, ?, ?, ?)',
                    UUID.randomUUID(),
                    patronId,
                    UUID.randomUUID(),
                    1,
                    null)
            latest.validate()

        then:
            noExceptionThrown()

        cleanup:
            database.shutdown()
    }

    private static def database() {
        new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.H2)
                .build()
    }

    private static Flyway flyway(DataSource dataSource, String location) {
        Flyway.configure()
                .dataSource(dataSource)
                .locations(location)
                .load()
    }
}
