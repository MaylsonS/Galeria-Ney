package com.pessoal.galeria_ney.controller;

import com.pessoal.galeria_ney.domain.Usuario;
import com.pessoal.galeria_ney.dto.UsuarioResponseDTO;
import com.pessoal.galeria_ney.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/artistas")
public class ArtistaController {

    private final UsuarioService service;

    public ArtistaController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        List<Usuario> artistas = service.listarTodos();
        List<UsuarioResponseDTO> resposta = artistas.stream().map(UsuarioResponseDTO::new).toList();
        return ResponseEntity.ok(resposta);
    }
}