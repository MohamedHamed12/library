package io.pillopl.library.lending.patronprofile.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface PatronStatusViewJpaRepository extends JpaRepository<PatronStatusViewEntity, Long> {

  Optional<PatronStatusViewEntity> findByPatronId(UUID patronId);
}

interface PatronHoldViewJpaRepository extends JpaRepository<PatronHoldViewEntity, Long> {

  List<PatronHoldViewEntity>
      findByHoldByPatronIdAndCheckedOutAtIsNullAndExpiredAtIsNullAndCanceledAtIsNull(
          UUID holdByPatronId);
}

interface PatronCheckoutViewJpaRepository extends JpaRepository<PatronCheckoutViewEntity, Long> {

  List<PatronCheckoutViewEntity> findByCheckedOutByPatronIdAndReturnedAtIsNull(
      UUID checkedOutByPatronId);
}
