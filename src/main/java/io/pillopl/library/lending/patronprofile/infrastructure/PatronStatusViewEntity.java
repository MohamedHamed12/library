package io.pillopl.library.lending.patronprofile.infrastructure;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

import io.pillopl.library.lending.patron.model.PatronStatus;

@Entity
@Immutable
@Table(name = "patron_database_entity")
class PatronStatusViewEntity {

  @Id private Long id;

  @Column(name = "patron_id")
  private UUID patronId;

  @Column(name = "status")
  private String status;

  protected PatronStatusViewEntity() {}

  PatronStatus toDomainStatus() {
    return PatronStatus.valueOf(status);
  }
}
