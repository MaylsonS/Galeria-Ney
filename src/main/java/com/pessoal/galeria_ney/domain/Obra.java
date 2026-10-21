package com.pessoal.galeria_ney.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.*;

@Entity(name = "tb_obras")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Obra {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoObra tipo;

    private String urlMidia;

    @ElementCollection
    @CollectionTable(name = "obra_midias", joinColumns = @JoinColumn(name = "obra_id"))
    @Column(name = "url")
    private List<String> midias = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "obra_curtidas", joinColumns = @JoinColumn(name = "obra_id"))
    @Column(name = "usuario_id")
    @Builder.Default
    private Set<UUID> curtidas = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "obra_tags", joinColumns = @JoinColumn(name = "obra_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    private LocalDate dataPostagem;

    private LocalDate dataAtualizacao;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "autor_id")
    private Usuario autor;

    public void adicionarTag(String tag) {}

    @PrePersist
    public void prePersist() {
        if(this.dataPostagem == null) this.dataPostagem = LocalDate.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDate.now();
    }
}