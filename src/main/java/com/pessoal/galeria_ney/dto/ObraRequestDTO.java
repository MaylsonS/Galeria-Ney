package com.pessoal.galeria_ney.dto;

import com.pessoal.galeria_ney.domain.Obra;
import com.pessoal.galeria_ney.domain.TipoObra;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ObraRequestDTO(
        @NotBlank(message = "O titulo é obrigatorio")
        String titulo,

        String descricao,

        @NotNull(message = "O tipo é obrigatório")
        TipoObra tipo,

        String urlMidia
) {
    public Obra toEntity() {
        return Obra.builder()
                .titulo(this.titulo)
                .descricao(this.descricao)
                .tipo(this.tipo)
                .urlMidia(this.urlMidia)
                .build();
    }
}
