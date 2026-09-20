package io.pillopl.library;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import com.tngtech.archunit.core.domain.JavaClass;

class SpringModulithArchitectureTest {

  private final ApplicationModules modules =
      ApplicationModules.of(
          LibraryApplication.class,
          JavaClass.Predicates.type(LendingConfig.class)
              .or(JavaClass.Predicates.type(LendingDatabaseConfig.class)));

  @Test
  void verifiesModuleBoundaries() {
    modules.verify();
  }

  @Test
  void writesModuleDocumentation() {
    new Documenter(modules).writeDocumentation();
  }
}
