package com.pessoal.galeria_ney.controller;


import com.pessoal.galeria_ney.domain.Obra;
import com.pessoal.galeria_ney.repository.ObraRepository;
import com.pessoal.galeria_ney.service.ObraService;
import jakarta.validation.Valid;
import org.hibernate.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/obras")
public class ObraController {
    private final ObraRepository repository;
    private final ObraService service;

    public ObraController(ObraRepository repository, ObraService service) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<Obra>> listar(@RequestParam(required = false) String termo) {
        List<Obra> obras = service.listar(termo);
        return ResponseEntity.ok(obras);
    }

    @PostMapping
    public ResponseEntity<Obra> salvar(@Valid @RequestBody Obra obra) {
        Obra obraSalva = service.cadastrar(obra);

        return ResponseEntity.status(HttpStatus.CREATED).body(obraSalva);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Obra> atualizarObra(@PathVariable UUID id,  @Valid @RequestBody Obra obra) {
        Obra obraAtualizado = service.atualizar(id, obra);
        return ResponseEntity.ok(obraAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Obra> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
