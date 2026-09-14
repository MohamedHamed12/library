package io.pillopl.library.database;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PostgreSQLDatabaseCleanupListener extends AbstractTestExecutionListener {

    private static final int ORDER = TransactionalTestExecutionListener.ORDER - 100;

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public void afterTestMethod(TestContext testContext) {
        testContext.getApplicationContext()
                .getBeansOfType(DataSource.class)
                .values()
                .forEach(this::clean);
    }

    private void clean(DataSource dataSource) {
        if (!isPostgreSQL(dataSource)) {
            return;
        }

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<String> tables = jdbcTemplate.queryForList("""
                SELECT quote_ident(schemaname) || '.' || quote_ident(tablename)
                FROM pg_tables
                WHERE schemaname = current_schema()
                  AND tablename <> 'flyway_schema_history'
                """, String.class);

        if (!tables.isEmpty()) {
            jdbcTemplate.execute("TRUNCATE TABLE " + String.join(", ", tables) + " RESTART IDENTITY CASCADE");
        }
    }

    private boolean isPostgreSQL(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return "PostgreSQL".equals(connection.getMetaData().getDatabaseProductName());
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not inspect integration-test database", exception);
        }
    }
}
