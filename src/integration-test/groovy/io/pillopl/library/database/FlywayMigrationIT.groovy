package io.pillopl.library.database

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationVersion
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.testcontainers.postgresql.PostgreSQLContainer
import spock.lang.Specification

import javax.sql.DataSource
import java.sql.Timestamp

@SpringBootTest(classes = PostgreSQLTestConfiguration)
class FlywayMigrationIT extends Specification {

    @Autowired
    PostgreSQLContainer postgreSQLContainer

    def 'creates catalogue schema from an empty database'() {
        given:
            def schema = schema('catalogue')
            def database = database(schema)
            def flyway = flyway(database, 'classpath:db/migration/catalogue', schema)
            def jdbc = new JdbcTemplate(database)

        when:
            flyway.migrate()
            jdbc.update(
                    "INSERT INTO ${schema}.catalogue_book_instance (isbn, book_id) VALUES (?, ?)",
                    '9780134685991',
                    UUID.randomUUID())

        then:
            jdbc.queryForObject("SELECT COUNT(*) FROM ${schema}.catalogue_book_instance", Integer) == 1
            jdbc.queryForObject("SELECT COUNT(*) FROM ${schema}.flyway_schema_history WHERE success = TRUE", Integer) == 1

        when:
            flyway.validate()

        then:
            noExceptionThrown()
    }

    def 'upgrades lending version one schema to the latest version'() {
        given:
            def schema = schema('lending')
            def database = database(schema)
            def jdbc = new JdbcTemplate(database)
            def versionOne = flyway(database, 'classpath:db/migration/lending', schema, MigrationVersion.fromVersion('1'))
            def latest = flyway(database, 'classpath:db/migration/lending', schema)
            def patronId = UUID.randomUUID()
            def holdTill = Timestamp.valueOf('2026-09-14 12:00:00')

        when:
            versionOne.migrate()
            jdbc.update(
                    "INSERT INTO ${schema}.patron_database_entity (patron_type, patron_id, email_address) VALUES (?, ?, ?)",
                    'Regular',
                    patronId,
                    'legacy@example.test')
            jdbc.update(
                    "INSERT INTO ${schema}.hold_database_entity (book_id, patron_id, library_branch_id, patron_database_entity, till) VALUES (?, ?, ?, ?, ?)",
                    UUID.randomUUID(),
                    patronId,
                    UUID.randomUUID(),
                    1,
                    holdTill)
            latest.migrate()

        then:
            jdbc.queryForObject("SELECT patron_id FROM ${schema}.patron_database_entity WHERE patron_id = ?", UUID, patronId) == patronId
            jdbc.queryForObject("SELECT status FROM ${schema}.patron_database_entity WHERE patron_id = ?", String, patronId) == 'ACTIVE'
            jdbc.queryForObject("SELECT till FROM ${schema}.hold_database_entity WHERE patron_id = ?", Timestamp, patronId) == holdTill
            jdbc.queryForObject("SELECT extension_count FROM ${schema}.hold_database_entity WHERE patron_id = ?", Integer, patronId) == 0
            jdbc.queryForObject("SELECT COUNT(*) FROM ${schema}.flyway_schema_history WHERE success = TRUE", Integer) == 3

        when:
            jdbc.update(
                    "INSERT INTO ${schema}.hold_database_entity (book_id, patron_id, library_branch_id, patron_database_entity, till) VALUES (?, ?, ?, ?, ?)",
                    UUID.randomUUID(),
                    patronId,
                    UUID.randomUUID(),
                    1,
                    null)
            latest.validate()

        then:
            noExceptionThrown()
    }

    private DataSource database(String schema) {
        def dataSource = new DriverManagerDataSource()
        dataSource.setDriverClassName('org.postgresql.Driver')
        dataSource.setUrl("${postgreSQLContainer.jdbcUrl}?currentSchema=${schema}")
        dataSource.setUsername(postgreSQLContainer.username)
        dataSource.setPassword(postgreSQLContainer.password)
        return dataSource
    }

    private static String schema(String prefix) {
        "${prefix}_${UUID.randomUUID().toString().replace('-', '')}"
    }

    private static Flyway flyway(DataSource dataSource, String location, String schema, MigrationVersion target = null) {
        def configuration = Flyway.configure()
                .dataSource(dataSource)
                .locations(location)
                .schemas(schema)
                .defaultSchema(schema)

        if (target != null) {
            configuration.target(target)
        }

        configuration.load()
    }
}
