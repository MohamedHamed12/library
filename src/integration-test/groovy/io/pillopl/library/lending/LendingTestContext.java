package io.pillopl.library.lending;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.pillopl.library.LendingConfig;
import io.pillopl.library.database.PostgreSQLTestConfiguration;

@Configuration
@Import({LendingConfig.class, PostgreSQLTestConfiguration.class})
public class LendingTestContext {}
