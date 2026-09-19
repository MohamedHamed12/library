package io.pillopl.library.lending.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = {"io.pillopl.library", "org.springframework"})
public class NoSpringInDomainLogicTest {

  @ArchTest
  public static final ArchRule model_should_not_depend_on_spring =
      noClasses()
          .that()
          .resideInAPackage("..io.pillopl.library.lending..model..")
          .and()
          .doNotHaveSimpleName("package-info")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("org.springframework..");

  @ArchTest
  public static final ArchRule application_should_not_access_spring =
      noClasses()
          .that()
          .resideInAPackage("..io.pillopl.library.lending..application..")
          .should()
          .accessClassesThat()
          .resideInAPackage("org.springframework..");
}
