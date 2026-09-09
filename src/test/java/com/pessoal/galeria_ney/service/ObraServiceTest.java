package com.pessoal.galeria_ney.service;

import com.pessoal.galeria_ney.domain.Obra;
import com.pessoal.galeria_ney.domain.TipoObra;
import com.pessoal.galeria_ney.domain.UserRole;
import com.pessoal.galeria_ney.domain.Usuario;
import com.pessoal.galeria_ney.infra.exception.RegraDeNegocioException;
import com.pessoal.galeria_ney.repository.ObraRepository;
import com.pessoal.galeria_ney.service.storage.MidiaStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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

    private Usuario usuarioFalso;

    @BeforeEach
    void setUp() {
        usuarioFalso = new Usuario("autor@teste.com", "123", UserRole.USER);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(usuarioFalso);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Deve cadastrar obra com imagem e retornar a obra salva")
    void cadastrarComImagemCenarioDeSucesso() {
        Obra obraInicial = Obra.builder().titulo("Teste").descricao("Uma arte").build();
        MockMultipartFile arquivoFalso = new MockMultipartFile("arquivo", "teste.png", "image/png", "conteudo".getBytes());

        when(storageService.upload(arquivoFalso)).thenReturn("https://cloudinary.com/foto_falsa.jpg");
        when(repository.save(any(Obra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Usa o nome atualizado do método que recebe o arquivo primeiro
        Obra obraSalva = obraService.cadastrarImagem(arquivoFalso, obraInicial);

        assertNotNull(obraSalva);
        assertEquals(TipoObra.IMAGEM, obraSalva.getTipo());
        assertEquals("https://cloudinary.com/foto_falsa.jpg", obraSalva.getUrlMidia());

        // Verifica se a regra de negócio nova funcionou: O autor foi associado?
        assertEquals(usuarioFalso, obraSalva.getAutor());

        verify(repository, times(1)).save(any(Obra.class));
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar cadastrar imagem vazia")
    void cadastrarComImagemVazia() {
        Obra obraInicial = Obra.builder().titulo("Teste").build();
        MockMultipartFile arquivoFalsoVazio = new MockMultipartFile("arquivo", new byte[0]);

        assertThrows(RegraDeNegocioException.class, () -> {
            obraService.cadastrarImagem(arquivoFalsoVazio, obraInicial);
        });

        verify(storageService, never()).upload(any());
        verify(repository, never()).save(any());
    }
}