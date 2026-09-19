package io.pillopl.library;

import java.time.Clock;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.modulith.Modulithic;

import io.pillopl.library.catalogue.CatalogueConfiguration;

@Modulithic(
    systemName = "Library",
    sharedModules = "io.pillopl.library.commons",
    useFullyQualifiedModuleNames = true)
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
public class LibraryApplication {

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  public static void main(String[] args) {
    new SpringApplicationBuilder()
        .parent(LibraryApplication.class)
        .child(LendingConfig.class)
        .web(WebApplicationType.SERVLET)
        .sibling(CatalogueConfiguration.class)
        .web(WebApplicationType.NONE)
        .run(args);
  }
}
