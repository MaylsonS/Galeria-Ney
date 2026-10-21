package com.pessoal.galeria_ney.service;

import com.pessoal.galeria_ney.domain.Obra;
import com.pessoal.galeria_ney.infra.exception.RegraDeNegocioException;
import com.pessoal.galeria_ney.repository.ObraRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InteracaoService {

    private final ObraRepository obraRepository;

    public InteracaoService(ObraRepository obraRepository) {
        this.obraRepository = obraRepository;
    }

//    RED
    public void curtirObra(UUID obraId, UUID usuarioId) {}

//    GREEN
//    public void curtirObra(UUID obraId, UUID usuarioId) {
//        Obra obra = obraRepository.findById(obraId).get();
//
//        // Código sujo, mas que faz o teste passar
//        if (obra.getCurtidas().contains(usuarioId)) {
//            throw new RegraDeNegocioException("curtida", "Você já curtiu esta obra");
//        }
//
//        obra.getCurtidas().add(usuarioId);
//        obraRepository.save(obra);
//    }

//    REFACTOR
//    public void curtirObra(UUID obraId, UUID usuarioId) {
//        Obra obra = obraRepository.findById(obraId)
//                .orElseThrow(() -> new RegraDeNegocioException("id", "Obra não encontrada"));
//
//        if (obra.getCurtidas().contains(usuarioId)) {
//            throw new RegraDeNegocioException("curtida", "Você já curtiu esta obra");
//        }
//
//        obra.getCurtidas().add(usuarioId);
//        obraRepository.save(obra);
//    }
}