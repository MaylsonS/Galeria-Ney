package com.pessoal.galeria_ney.controller;


import com.pessoal.galeria_ney.domain.Obra;
import com.pessoal.galeria_ney.domain.TipoObra;
import com.pessoal.galeria_ney.dto.ObraRequestDTO;
import com.pessoal.galeria_ney.dto.ObraResponseDTO;
import com.pessoal.galeria_ney.service.ObraService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ObraResponseDTO> salvar(@Valid @RequestBody ObraRequestDTO dados) {
        Obra obraParaSalvar = dados.toEntity();

        Obra obraSalva = service.cadastrar(obraParaSalvar);


        return ResponseEntity.status(HttpStatus.CREATED).body(new ObraResponseDTO(obraSalva));
    }

    @PostMapping(value = "/imagem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ObraResponseDTO> salvarImagem(
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestPart("arquivo") MultipartFile arquivo) {

        Obra obra = Obra.builder()
                .titulo(titulo)
                .descricao(descricao)
                .tipo(TipoObra.IMAGEM)
                .build();

        Obra obraSalva = service.cadastrarImagem(arquivo,obra);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ObraResponseDTO(obraSalva));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ObraResponseDTO> atualizarObra(@PathVariable UUID id,  @Valid @RequestBody ObraRequestDTO dados) {
        Obra obraAtualizado = service.atualizar(id, dados.toEntity());

        return ResponseEntity.ok(new ObraResponseDTO(obraAtualizado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Obra> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
