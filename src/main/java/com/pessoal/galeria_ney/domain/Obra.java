package com.pessoal.galeria_ney.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity(name = "tb_obras")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Obra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O título é obrigatório")
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @NotNull(message = "O tipo é obrigatório")
    @Enumerated(EnumType.STRING)
    private TipoObra tipo;

    @NotBlank(message = "O link/arquivo é obrigatório")
    private String urlMidia;

    private LocalDate dataPostagem;
}
