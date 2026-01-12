package com.pessoal.galeria_ney.service;

import com.pessoal.galeria_ney.domain.Obra;
import com.pessoal.galeria_ney.domain.TipoObra;
import com.pessoal.galeria_ney.infra.exception.RegraDeNegocioException;
import com.pessoal.galeria_ney.repository.ObraRepository;
import org.springframework.stereotype.Service;

@Service
public class ObraService {

    private final ObraRepository repository;

    public ObraService(ObraRepository repository) {
        this.repository = repository;
    }

    public Obra cadastrar(Obra obra) {
        if (obra.getTipo() != TipoObra.IMAGEM) {
            if (obra.getUrlMidia() == null || obra.getUrlMidia().isBlank()) {
                throw new RegraDeNegocioException("urlMidia", "Para vídeos ou músicas, o link é obrigatório!");
            }
        }

        return repository.save(obra);
    }
}