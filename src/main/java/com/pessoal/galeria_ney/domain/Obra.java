package com.pessoal.galeria_ney.domain;

import jakarta.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 20)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoObra tipo;

    private String urlMidia;

    private LocalDate dataPostagem;

    private LocalDate dataAtualizacao;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    @PrePersist
    public void prePersist() {
        if(this.dataPostagem == null) this.dataPostagem = LocalDate.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDate.now();
    }
}