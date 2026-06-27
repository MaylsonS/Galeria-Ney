package com.pessoal.galeria_ney.service;

import com.pessoal.galeria_ney.domain.Obra;
import com.pessoal.galeria_ney.domain.TipoObra;
import com.pessoal.galeria_ney.infra.exception.RegraDeNegocioException;
import com.pessoal.galeria_ney.repository.ObraRepository;
import com.pessoal.galeria_ney.service.storage.MidiaStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObraServiceTest {

    @Mock
    private ObraRepository repository;

    @Mock
    private MidiaStorageService storageService;

    @InjectMocks
    private ObraService obraService;

    @Test
    @DisplayName("Deve cadastrar obra com imagem e retornar a obra salva")
    void cadastrarComImagemCenarioDeSucesso() {
        // Arrange: Preparar o cenário
        Obra obraInicial = Obra.builder().titulo("Teste").descricao("Uma arte").build();
        MockMultipartFile arquivoFalso = new MockMultipartFile("arquivo", "teste.png", "image/png", "conteudo".getBytes());

        // Ensinamos os dublês a como se comportar
        when(storageService.upload(arquivoFalso)).thenReturn("https://cloudinary.com/foto_falsa.jpg");
        when(repository.save(any(Obra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Executar a ação
        Obra obraSalva = obraService.cadatrarImagem(arquivoFalso,obraInicial);

        // Assert: Verificar os resultados
        assertNotNull(obraSalva);
        assertEquals(TipoObra.IMAGEM, obraSalva.getTipo());
        assertEquals("https://cloudinary.com/foto_falsa.jpg", obraSalva.getUrlMidia());

        // Verifica se o repositório foi de facto chamado uma vez
        verify(repository, times(1)).save(any(Obra.class));
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar cadastrar imagem vazia")
    void cadastrarComImagemVazia() {
        // Arrange
        Obra obraInicial = Obra.builder().titulo("Teste").build();
        MockMultipartFile arquivoFalsoVazio = new MockMultipartFile("arquivo", new byte[0]);

        // Act & Assert
        assertThrows(RegraDeNegocioException.class, () -> {
            obraService.cadatrarImagem(arquivoFalsoVazio, obraInicial);
        });

        // Garante que se o arquivo for vazio, ele JAMAIS chama o upload ou o banco
        verify(storageService, never()).upload(any());
        verify(repository, never()).save(any());
    }
}