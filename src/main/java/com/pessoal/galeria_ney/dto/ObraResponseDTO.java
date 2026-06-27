package com.pessoal.galeria_ney.dto;

import com.pessoal.galeria_ney.domain.Obra;
import com.pessoal.galeria_ney.domain.TipoObra;
import com.pessoal.galeria_ney.infra.utils.EmbedUrlResolver;

import java.time.LocalDate;
import java.util.UUID;

public record ObraResponseDTO (
        UUID id,
        String titulo,
        String descricao,
        TipoObra tipo,
        String urlMidia,
        String urlEmbed,
        LocalDate dataPostagem
){
    public ObraResponseDTO(Obra obra) {
        this(
                obra.getId(),
                obra.getTitulo(),
                obra.getDescricao(),
                obra.getTipo(),
                obra.getUrlMidia(),
                EmbedUrlResolver.getEmbedUrl(obra.getUrlMidia(), obra.getTipo()),
                obra.getDataPostagem()
        );
    }
}