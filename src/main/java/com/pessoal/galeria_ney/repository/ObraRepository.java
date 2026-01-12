package com.pessoal.galeria_ney.repository;

import com.pessoal.galeria_ney.domain.Obra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ObraRepository extends JpaRepository<Obra, UUID> {

}