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
    @DisplayName("Deve cadastrar obra com imagem e retornar a obra salva")
    void cadastrarComImagemCenarioDeSucesso() {
        Obra obraInicial = Obra.builder().titulo("Teste").descricao("Uma arte").build();
        MockMultipartFile arquivoFalso = new MockMultipartFile("arquivo", "teste.png", "image/png", "conteudo".getBytes());

        when(storageService.upload(arquivoFalso)).thenReturn("https://cloudinary.com/foto_falsa.jpg");
        when(repository.save(any(Obra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Obra obraSalva = obraService.cadastrarImagem(arquivoFalso, obraInicial);

        assertNotNull(obraSalva);
        assertEquals(TipoObra.IMAGEM, obraSalva.getTipo());
        assertEquals("https://cloudinary.com/foto_falsa.jpg", obraSalva.getUrlMidia());

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


    // Tecnica Valor limite

    @Test
    @DisplayName("Deve cadastrar obra com titulo de exato 20 caracteres (Limite Superior Valido)")
    void cadastrarObraTituloLimiteSuperiorValido() {
        String titulo20Caracteres = "A".repeat(20);

        Obra obraLimiteValido = Obra.builder()
                .titulo(titulo20Caracteres)
                .tipo(TipoObra.VIDEO_YOUTUBE)
                .urlMidia("https://www.youtube.com/watch?v=123")
                .build();

        when(repository.save(any(Obra.class))).thenAnswer(i -> i.getArgument(0));

        Obra obraSalva = obraService.cadastrar(obraLimiteValido);

        assertNotNull(obraSalva);
        assertEquals(20, obraSalva.getTitulo().length());
        assertEquals(titulo20Caracteres, obraSalva.getTitulo());
    }

    @Test
    @DisplayName("Deve falhar ao cadastrar obra com titulo de 21 caracteres (Limite Superior Invalido)")
    void cadastrarObraTituloLimiteSuperiorInvalido() {
        String titulo21Caracteres = "A".repeat(21);
        Obra obraLimiteInvalido = Obra.builder().titulo(titulo21Caracteres).build();

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> {
            obraService.cadastrar(obraLimiteInvalido);
        });

        assertEquals("O título deve ter no máximo 20 caracteres", ex.getMessage());
        verify(repository, never()).save(any());
    }


    @Test
    @DisplayName("Deve falhar ao cadastrar imagem com arquivo de 0 bytes (Limite Inferior Arquivo)")
    void cadastrarImagemArquivoLimiteInferiorInvalido() {

        Obra obra = Obra.builder().titulo("Arte").build();
        MockMultipartFile arquivo0Bytes = new MockMultipartFile("arquivo", "arte.jpg", "image/jpeg", new byte[0]);

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> {
            obraService.cadastrarImagem(arquivo0Bytes, obra);
        });

        assertEquals("O arquivo da imagem é obrigatório.", ex.getMessage());
        verify(storageService, never()).upload(any());
    }

    @Test
    @DisplayName("Deve cadastrar imagem com arquivo de exato 1 byte (Limite Inferior Arquivo Valido)")
    void cadastrarImagemArquivoLimiteInferiorValido() {

        Obra obra = Obra.builder().titulo("Arte").build();
        MockMultipartFile arquivo1Byte = new MockMultipartFile("arquivo", "arte.jpg", "image/jpeg", new byte[1]);

        when(storageService.upload(arquivo1Byte)).thenReturn("https://cloudinary.com/1byte.jpg");
        when(repository.save(any(Obra.class))).thenAnswer(i -> i.getArgument(0));

        Obra obraSalva = obraService.cadastrarImagem(arquivo1Byte, obra);

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
        Obra obra = Obra.builder().autor(autorDaObra).build();

        when(repository.findById(idObra)).thenReturn(java.util.Optional.of(obra));

        if (esperaExcecao) {
            RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> {
                obraService.excluir(idObra);
            });
            assertEquals("Acesso Negado: Você só pode editar ou excluir as suas próprias obras.", ex.getMessage());
        } else {
            assertDoesNotThrow(() -> {
                obraService.excluir(idObra);
            });
        }
    }




}