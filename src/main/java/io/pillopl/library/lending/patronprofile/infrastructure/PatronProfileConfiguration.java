package io.pillopl.library.lending.patronprofile.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.pillopl.library.lending.patronprofile.model.PatronProfiles;

@Configuration
public class PatronProfileConfiguration {

  @Bean
  public PatronProfiles patronProfilesReadModel(
      PatronStatusViewJpaRepository patrons,
      PatronHoldViewJpaRepository holds,
      PatronCheckoutViewJpaRepository checkouts) {
    return new PatronProfileReadModel(patrons, holds, checkouts);
  }
}
