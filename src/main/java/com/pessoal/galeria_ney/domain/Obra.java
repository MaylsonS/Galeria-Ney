package com.pessoal.galeria_ney.domain;

import com.pessoal.galeria_ney.infra.exception.RegraDeNegocioException;
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

//    RED
//    public void adicionarTag(String tag) {}

//    GREEN
//    public void adicionarTag(String tag) {
//        if (this.tags.size() == 3) {
//            throw new RegraDeNegocioException("tag", "A obra já possui o limite máximo de 3 tags");
//        }
//        this.tags.add(tag);
//    }

//REFACTOR
@Transient
private static final int LIMITE_MAXIMO_TAGS = 3;
    public void adicionarTag(String tag) {
        if (this.tags.size() >= LIMITE_MAXIMO_TAGS) {
            throw new RegraDeNegocioException("tag", "A obra já possui o limite máximo de " + LIMITE_MAXIMO_TAGS + " tags");
        }
        this.tags.add(tag.trim().toLowerCase());
    }
    @PrePersist
    public void prePersist() {
        if(this.dataPostagem == null) this.dataPostagem = LocalDate.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dataAtualizacao = LocalDate.now();
    }
}