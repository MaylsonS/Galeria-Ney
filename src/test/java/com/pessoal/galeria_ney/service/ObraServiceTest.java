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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    @DisplayName("Deve cadastrar obra com imagem e retornar a obra salva com status ativo")
    void cadastrarComImagemCenarioDeSucesso() {
        Obra obraInicial = Obra.builder().titulo("Teste").descricao("Uma arte").build();
        MockMultipartFile arquivoFalso = new MockMultipartFile("arquivo", "teste.png", "image/png", "conteudo".getBytes());

        when(storageService.upload(arquivoFalso)).thenReturn("https://cloudinary.com/foto_falsa.jpg");
        when(repository.save(any(Obra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Obra obraSalva = obraService.cadastrarImagem(List.of(arquivoFalso), obraInicial);

        assertNotNull(obraSalva);
        assertEquals(TipoObra.IMAGEM, obraSalva.getTipo());
        assertEquals("https://cloudinary.com/foto_falsa.jpg", obraSalva.getUrlMidia());
        assertTrue(obraSalva.isAtivo(), "A obra deve ser salva como ativa por defeito");
        assertEquals(usuarioFalso, obraSalva.getAutor());

        verify(repository, times(1)).save(any(Obra.class));
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar cadastrar imagem vazia")
    void cadastrarComImagemVazia() {
        Obra obraInicial = Obra.builder().titulo("Teste").build();

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> {
            obraService.cadastrarImagem(Collections.emptyList(), obraInicial);
        });

        assertEquals("É obrigatório enviar pelo menos um arquivo de mídia.", ex.getMessage());
        verify(storageService, never()).upload(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve cadastrar obra com titulo de exato 50 caracteres (Limite Superior Valido)")
    void cadastrarObraTituloLimiteSuperiorValido() {
        // Atualizado para 50 caracteres
        String titulo50Caracteres = "A".repeat(50);

        Obra obraLimiteValido = Obra.builder()
                .titulo(titulo50Caracteres)
                .tipo(TipoObra.VIDEO_YOUTUBE)
                .urlMidia("https://www.youtube.com/watch?v=123")
                .build();

        when(repository.save(any(Obra.class))).thenAnswer(i -> i.getArgument(0));

        Obra obraSalva = obraService.cadastrar(obraLimiteValido);

        assertNotNull(obraSalva);
        assertEquals(50, obraSalva.getTitulo().length());
        assertEquals(titulo50Caracteres, obraSalva.getTitulo());
    }

    @Test
    @DisplayName("Deve falhar ao cadastrar obra com titulo de 51 caracteres (Limite Superior Invalido)")
    void cadastrarObraTituloLimiteSuperiorInvalido() {
        // Atualizado para 51 caracteres
        String titulo51Caracteres = "A".repeat(51);
        Obra obraLimiteInvalido = Obra.builder().titulo(titulo51Caracteres).build();

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> {
            obraService.cadastrar(obraLimiteInvalido);
        });

        // Atualizada a mensagem esperada
        assertEquals("O título deve ter no máximo 50 caracteres", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao cadastrar arquivo com tipo invalido (Ex: PDF)")
    void cadastrarImagemArquivoInvalido() {
        Obra obra = Obra.builder().titulo("Arte").build();
        MockMultipartFile arquivoInvalido = new MockMultipartFile("arquivo", "documento.pdf", "application/pdf", new byte[10]);

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> {
            obraService.cadastrarImagem(List.of(arquivoInvalido), obra);
        });

        assertEquals("O arquivo enviado não é uma mídia válida.", ex.getMessage());
        verify(storageService, never()).upload(any());
    }

    @Test
    @DisplayName("Deve cadastrar imagem com arquivo de exato 1 byte (Limite Inferior Arquivo Valido)")
    void cadastrarImagemArquivoLimiteInferiorValido() {
        Obra obra = Obra.builder().titulo("Arte").build();
        MockMultipartFile arquivo1Byte = new MockMultipartFile("arquivo", "arte.jpg", "image/jpeg", new byte[1]);

        when(storageService.upload(arquivo1Byte)).thenReturn("https://cloudinary.com/1byte.jpg");
        when(repository.save(any(Obra.class))).thenAnswer(i -> i.getArgument(0));

        Obra obraSalva = obraService.cadastrarImagem(List.of(arquivo1Byte), obra);

        assertNotNull(obraSalva);
        assertEquals("https://cloudinary.com/1byte.jpg", obraSalva.getUrlMidia());
        verify(storageService, times(1)).upload(arquivo1Byte);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({
            "true,  true,  false", // Regra 1: É Admin (V), É Autor (V) -> Permite (Não lança exceção)
            "true,  false, false", // Regra 2: É Admin (V), É Autor (F) -> Permite
            "false, true,  false", // Regra 3: É Admin (F), É Autor (V) -> Permite
            "false, false, true"   // Regra 4: É Admin (F), É Autor (F) -> RegraDeNegocioException
    })
    @DisplayName("Tabela de Decisão: verificarPermissao via excluir() cruzando Admin e Autor")
    void testarVerificarPermissaoTabelaDeDecisao(boolean isAdmin, boolean isAutor, boolean esperaExcecao) {

        UUID idLogado = UUID.randomUUID();
        UserRole roleDoLogado = isAdmin ? UserRole.ADMIN : UserRole.USER;
        Usuario usuarioLogado = new Usuario("logado@teste.com", "123", roleDoLogado);
        usuarioLogado.setId(idLogado);

        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getPrincipal()).thenReturn(usuarioLogado);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UUID idDonoDaObra = isAutor ? idLogado : UUID.randomUUID();
        Usuario autorDaObra = new Usuario("autor@teste.com", "123", UserRole.USER);
        autorDaObra.setId(idDonoDaObra);

        UUID idObra = UUID.randomUUID();
        Obra obra = Obra.builder()
                .autor(autorDaObra)
                .ativo(true)
                .build();

        when(repository.findById(idObra)).thenReturn(Optional.of(obra));

        if (esperaExcecao) {
            RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> {
                obraService.excluir(idObra);
            });
            assertEquals("Acesso Negado: Você só pode editar ou excluir as suas próprias obras.", ex.getMessage());

            assertTrue(obra.isAtivo());
            verify(repository, never()).save(any());
        } else {
            assertDoesNotThrow(() -> {
                obraService.excluir(idObra);
            });

            assertFalse(obra.isAtivo(), "A flag ativo deve ser alterada para false.");
            verify(repository, times(1)).save(obra);
        }
    }
}