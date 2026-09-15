package io.pillopl.library.database

import io.pillopl.library.lending.LendingTestContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.Specification

import javax.sql.DataSource

@SpringBootTest(classes = LendingTestContext.class)
class PostgreSQLDatabaseIT extends Specification {

    @Autowired
    DataSource dataSource

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    PlatformTransactionManager transactionManager

    def 'uses PostgreSQL and runs lending migrations'() {
        expect:
            databaseProductName() == 'PostgreSQL'
            jdbcTemplate.queryForObject('SELECT COUNT(*) FROM flyway_schema_history WHERE success = TRUE', Integer) == 3
    }

    def 'persists UUID values and nullable open ended hold timestamps'() {
        given:
            def patronId = UUID.randomUUID()
            jdbcTemplate.update(
                    'INSERT INTO patron_database_entity (patron_type, patron_id, email_address) VALUES (?, ?, ?)',
                    'Regular',
                    patronId,
                    "${patronId}@example.test")
            def patronDatabaseId = jdbcTemplate.queryForObject(
                    'SELECT id FROM patron_database_entity WHERE patron_id = ?',
                    Integer,
                    patronId)

        when:
            jdbcTemplate.update(
                    'INSERT INTO hold_database_entity (book_id, patron_id, library_branch_id, patron_database_entity, till) VALUES (?, ?, ?, ?, ?)',
                    UUID.randomUUID(),
                    patronId,
                    UUID.randomUUID(),
                    patronDatabaseId,
                    null)

        then:
            jdbcTemplate.queryForObject(
                    'SELECT patron_id FROM hold_database_entity WHERE patron_id = ?',
                    UUID,
                    patronId) == patronId
            jdbcTemplate.queryForObject(
                    'SELECT COUNT(*) FROM hold_database_entity WHERE patron_id = ? AND till IS NULL',
                    Integer,
                    patronId) == 1
    }

    def 'enforces existing unique constraints'() {
        given:
            def patronId = UUID.randomUUID()
            jdbcTemplate.update(
                    'INSERT INTO patron_database_entity (patron_type, patron_id, email_address) VALUES (?, ?, ?)',
                    'Regular',
                    patronId,
                    'first@example.test')

        when:
            jdbcTemplate.update(
                    'INSERT INTO patron_database_entity (patron_type, patron_id, email_address) VALUES (?, ?, ?)',
                    'Regular',
                    patronId,
                    'second@example.test')

        then:
            thrown(DataIntegrityViolationException)
    }

    def 'rolls back database transactions'() {
        given:
            def patronId = UUID.randomUUID()
            def transactionTemplate = new TransactionTemplate(transactionManager)

        when:
            transactionTemplate.executeWithoutResult { status ->
                jdbcTemplate.update(
                        'INSERT INTO patron_database_entity (patron_type, patron_id, email_address) VALUES (?, ?, ?)',
                        'Regular',
                        patronId,
                        'rollback@example.test')
                status.setRollbackOnly()
            }

        then:
            jdbcTemplate.queryForObject(
                    'SELECT COUNT(*) FROM patron_database_entity WHERE patron_id = ?',
                    Integer,
                    patronId) == 0
    }

    private String databaseProductName() {
        def connection = dataSource.connection
        try {
            return connection.metaData.databaseProductName
        } finally {
            connection.close()
        }
    }
}
