package com.pessoal.galeria_ney.repository;

import com.pessoal.galeria_ney.domain.Obra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ObraRepository extends JpaRepository<Obra, UUID> {

    Page<Obra> findByAtivoTrue(Pageable pageable);

    Page<Obra> findByTituloContainingIgnoreCaseAndAtivoTrue(String termo, Pageable pageable);

    Page<Obra> findByAutorIdAndAtivoTrue(UUID autorId, Pageable pageable);
}