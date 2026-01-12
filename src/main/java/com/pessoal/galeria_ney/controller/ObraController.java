package com.pessoal.galeria_ney.controller;


import com.pessoal.galeria_ney.domain.Obra;
import com.pessoal.galeria_ney.repository.ObraRepository;
import com.pessoal.galeria_ney.service.ObraService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<Obra>> listar() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping
    public ResponseEntity<Obra> salvar(@Valid @RequestBody Obra obra) {
        Obra obraSalva = service.cadastrar(obra);

        return ResponseEntity.status(HttpStatus.CREATED).body(obraSalva);
    }
}
