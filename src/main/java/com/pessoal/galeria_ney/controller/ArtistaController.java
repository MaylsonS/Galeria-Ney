package com.pessoal.galeria_ney.controller;

import com.pessoal.galeria_ney.domain.Usuario;
import com.pessoal.galeria_ney.dto.UsuarioResponseDTO;
import com.pessoal.galeria_ney.repository.UsuarioRepository;
import com.pessoal.galeria_ney.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/artistas")
public class ArtistaController {

    private final UsuarioService service;
    private final UsuarioRepository repository;

    public ArtistaController(UsuarioService service, UsuarioRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        List<Usuario> artistas = service.listarTodos();
        List<UsuarioResponseDTO> resposta = artistas.stream().map(UsuarioResponseDTO::new).toList();
        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable UUID id) {
        return repository.findById(id)
                .map(usuario -> ResponseEntity.ok(new UsuarioResponseDTO(usuario)))
                .orElse(ResponseEntity.notFound().build());
    }
}