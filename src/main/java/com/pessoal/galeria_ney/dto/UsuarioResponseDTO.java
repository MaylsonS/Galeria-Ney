package com.pessoal.galeria_ney.dto;

import com.pessoal.galeria_ney.domain.Usuario;
import java.util.UUID;

public record UsuarioResponseDTO(
        UUID id,
        String login,
        String descricao,
        String fotoPerfil
) {
    public UsuarioResponseDTO(Usuario usuario) {
        this(usuario.getId(), usuario.getLogin(), usuario.getDescricao(), usuario.getFotoPerfil());
    }
}