package com.pessoal.galeria_ney.service;

import com.pessoal.galeria_ney.domain.Obra;
import com.pessoal.galeria_ney.infra.exception.RegraDeNegocioException;
import com.pessoal.galeria_ney.repository.ObraRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InteracaoServiceTest {

    @Mock
    private ObraRepository obraRepository;

    @InjectMocks
    private InteracaoService interacaoService;

    @Test
    @DisplayName("CT01 - Deve impedir que o mesmo usuário curta a obra duas vezes")
    void naoDevePermitirCurtidaDuplicada() {
        UUID usuarioId = UUID.randomUUID();
        UUID obraId = UUID.randomUUID();

        Obra obra = Obra.builder().id(obraId).build();
        obra.getCurtidas().add(usuarioId); // Simulando que o usuário já curtiu

        when(obraRepository.findById(obraId)).thenReturn(Optional.of(obra));

        // A execução: tenta curtir de novo
        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> {
            interacaoService.curtirObra(obraId, usuarioId);
        });

        assertEquals("Você já curtiu esta obra", ex.getMessage());
    }
}