package com.pessoal.galeria_ney.dto;

import com.pessoal.galeria_ney.domain.Obra;
import com.pessoal.galeria_ney.domain.TipoObra;

import java.time.LocalDate;
import java.util.UUID;

public record ObraResponseDTO (
        UUID id,
        String titulo,
        String descricao,
        TipoObra tipo,
        String urlMidia,
        LocalDate dataPostagem
){
    public ObraResponseDTO(Obra obra) {
        this(
                obra.getId(),
                obra.getTitulo(),
                obra.getDescricao(),
                obra.getTipo(),
                obra.getUrlMidia(),
                obra.getDataPostagem()
        );
    }
}
