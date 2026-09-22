package io.pillopl.library.catalogue;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface CatalogueBookJpaRepository extends JpaRepository<CatalogueBookEntity, Integer> {

  Optional<CatalogueBookEntity> findByIsbn(String isbn);
}

interface CatalogueBookInstanceJpaRepository
    extends JpaRepository<CatalogueBookInstanceEntity, Integer> {}
