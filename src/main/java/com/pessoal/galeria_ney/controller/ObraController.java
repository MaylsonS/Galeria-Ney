package com.pessoal.galeria_ney.controller;


import com.pessoal.galeria_ney.domain.Obra;
import com.pessoal.galeria_ney.dto.ObraRequestDTO;
import com.pessoal.galeria_ney.dto.ObraResponseDTO;
import com.pessoal.galeria_ney.repository.ObraRepository;
import com.pessoal.galeria_ney.service.ObraService;
import jakarta.validation.Valid;
import org.hibernate.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/obras")
public class ObraController {

    private final ObraService service;

    public ObraController(ObraService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ObraResponseDTO>> listar(@RequestParam(required = false) String termo) {
        List<Obra> obras = service.listar(termo);

        List<ObraResponseDTO> resposta = obras.stream()
                .map(ObraResponseDTO::new)
                .toList();

        return ResponseEntity.ok(resposta);
    }

    @PostMapping
    public ResponseEntity<ObraResponseDTO> salvar(@Valid @RequestBody ObraRequestDTO dados) {
        Obra obraParaSalvar = dados.toEntity();

        Obra obraSalva = service.cadastrar(obraParaSalvar);


        return ResponseEntity.status(HttpStatus.CREATED).body(new ObraResponseDTO(obraSalva));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ObraResponseDTO> atualizarObra(@PathVariable UUID id,  @Valid @RequestBody ObraRequestDTO dados) {
        Obra obraAtualizado = service.atualizar(id, dados.toEntity());

        return ResponseEntity.ok(new ObraResponseDTO(obraAtualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Obra> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
