package com.pessoal.galeria_ney.repository;

import com.pessoal.galeria_ney.domain.Obra;
import com.pessoal.galeria_ney.domain.TipoObra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ObraRepository extends JpaRepository<Obra, UUID> {

    Page<Obra> findByAtivoTrue(Pageable pageable);

    Page<Obra> findByTituloContainingIgnoreCaseAndAtivoTrue(String termo, Pageable pageable);

    // 1. Sem filtros adicionais
    Page<Obra> findByAutorIdAndAtivoTrue(UUID autorId, Pageable pageable);

    // 2. Só com o filtro de Tipo
    Page<Obra> findByAutorIdAndTipoAndAtivoTrue(UUID autorId, TipoObra tipo, Pageable pageable);

    // 3. Só com o filtro de Título (Busca de texto)
    Page<Obra> findByAutorIdAndTituloContainingIgnoreCaseAndAtivoTrue(UUID autorId, String titulo, Pageable pageable);

    // 4. Com ambos os filtros (Tipo e Texto)
    Page<Obra> findByAutorIdAndTipoAndTituloContainingIgnoreCaseAndAtivoTrue(UUID autorId, TipoObra tipo, String titulo, Pageable pageable);
}