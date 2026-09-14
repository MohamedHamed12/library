package io.pillopl.library.lending;

import io.pillopl.library.database.PostgreSQLTestConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({LendingConfig.class, PostgreSQLTestConfiguration.class})
public class LendingTestContext {
}
