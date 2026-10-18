package com.pessoal.galeria_ney.controller;

import com.pessoal.galeria_ney.domain.Obra;
import com.pessoal.galeria_ney.domain.TipoObra;
import com.pessoal.galeria_ney.dto.ObraRequestDTO;
import com.pessoal.galeria_ney.dto.ObraResponseDTO;
import com.pessoal.galeria_ney.repository.ObraRepository;
import com.pessoal.galeria_ney.service.ObraService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/obras")
public class ObraController {

    private final ObraService service;
    private final ObraRepository repository;

    public ObraController(ObraService service, ObraRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<Page<ObraResponseDTO>> listar(
            @RequestParam(required = false) String termo,
            @PageableDefault(size = 12, sort = "dataPostagem", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Obra> obras = service.listar(termo, pageable);
        Page<ObraResponseDTO> resposta = obras.map(ObraResponseDTO::new);
        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/autor/{autorId}")
    public ResponseEntity<Page<ObraResponseDTO>> listarPorAutor(
            @PathVariable UUID autorId,
            @RequestParam(required = false) TipoObra tipo,  // Recebe IMAGEM, VIDEO_YOUTUBE...
            @RequestParam(required = false) String termo,   // Recebe o texto da busca
            @PageableDefault(size = 12, sort = "dataPostagem", direction = Sort.Direction.DESC) Pageable pageable) {

        // Você precisará garantir que o seu ObraService e ObraRepository
        // saibam lidar com esses parâmetros de filtro na query do banco de dados.
        Page<Obra> obras = service.listarPorAutorFiltrado(autorId, tipo, termo, pageable);
        Page<ObraResponseDTO> resposta = obras.map(ObraResponseDTO::new);
        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ObraResponseDTO> buscarPorId(@PathVariable UUID id) {
        return repository.findById(id)
                .map(obra -> ResponseEntity.ok(new ObraResponseDTO(obra)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ObraResponseDTO> salvar(@Valid @RequestBody ObraRequestDTO dados) {
        Obra obraSalva = service.cadastrar(dados.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ObraResponseDTO(obraSalva));
    }

    @PostMapping(value = "/imagem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ObraResponseDTO> salvarImagem(
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestPart("arquivos") java.util.List<MultipartFile> arquivos) { // ALTERADO PARA LISTA

        Obra obra = Obra.builder()
                .titulo(titulo)
                .descricao(descricao)
                .tipo(TipoObra.IMAGEM)
                .build();

        Obra obraSalva = service.cadastrarImagem(arquivos, obra);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ObraResponseDTO(obraSalva));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ObraResponseDTO> atualizarObra(@PathVariable UUID id, @Valid @RequestBody ObraRequestDTO dados) {
        Obra obraAtualizada = service.atualizar(id, dados.toEntity());
        return ResponseEntity.ok(new ObraResponseDTO(obraAtualizada));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}