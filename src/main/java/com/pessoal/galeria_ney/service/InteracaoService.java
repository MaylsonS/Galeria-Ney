package com.pessoal.galeria_ney.service;

import com.pessoal.galeria_ney.repository.ObraRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InteracaoService {

    private final ObraRepository obraRepository;

    public InteracaoService(ObraRepository obraRepository) {
        this.obraRepository = obraRepository;
    }

    public void curtirObra(UUID obraId, UUID usuarioId) {

    }
}