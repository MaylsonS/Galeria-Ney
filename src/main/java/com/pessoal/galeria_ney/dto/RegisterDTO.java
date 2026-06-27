package com.pessoal.galeria_ney.dto;

import com.pessoal.galeria_ney.domain.UserRole;

public record RegisterDTO(
        String login,
        String senha,
        UserRole role
) {}