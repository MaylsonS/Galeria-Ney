package com.pessoal.galeria_ney.controller;

import com.pessoal.galeria_ney.domain.TipoObra;
import com.pessoal.galeria_ney.domain.UserRole;
import com.pessoal.galeria_ney.domain.Usuario;
import com.pessoal.galeria_ney.repository.ObraRepository;
import com.pessoal.galeria_ney.service.storage.MidiaStorageService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ObraControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MidiaStorageService storageService;

    @Autowired
    private ObraRepository repository;

    @BeforeEach
    void setupSecurityContext() {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(UUID.randomUUID());
        usuarioMock.setRole(UserRole.USER);
        usuarioMock.setLogin("test@user.com");

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(usuarioMock, null, usuarioMock.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Sucesso 1: Deve criar obra com arquivo e título nos limites mínimos (1 byte / 1 char)")
    void deveRetornar201_Sucesso1_EnvioMultipartNoLimiteMinimo() throws Exception {
        when(storageService.upload(any())).thenReturn("https://cloudinary.com/mock.jpg");

        // Alterado de "arquivo" para "arquivos"
        MockMultipartFile arquivo1Byte = new MockMultipartFile(
                "arquivos", "teste.jpg", "image/jpeg", new byte[1]
        );

        mockMvc.perform(multipart("/obras/imagem")
                        .file(arquivo1Byte)
                        .param("titulo", "A"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Sucesso 2: Deve retornar 200 OK acionando paginação quando termo for vazio")
    void deveRetornar200_Sucesso2_BuscaNoLimiteVazio() throws Exception {
        mockMvc.perform(get("/obras")
                        .param("termo", "")
                        .param("page", "0")
                        .param("size", "12"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Erro 1: Deve bloquear upload retornando 400 quando o arquivo enviado for vazio (0 bytes)")
    void deveRetornar400_Erro1_AusenciaDePayloadNoLimite() throws Exception {
        // Alterado de "arquivo" para "arquivos"
        MockMultipartFile arquivoVazio = new MockMultipartFile(
                "arquivos", "teste.jpg", "image/jpeg", new byte[0]
        );

        mockMvc.perform(multipart("/obras/imagem")
                        .file(arquivoVazio)
                        .param("titulo", "Obra Sem Foto"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Erro 2: Deve bloquear upload retornando 400 na ausência da barra requerida no Content-Type")
    void deveRetornar400_Erro2_LimiteDaRegraDeContentType() throws Exception {
        // Alterado de "arquivo" para "arquivos"
        MockMultipartFile arquivoContentTypeInvalido = new MockMultipartFile(
                "arquivos", "teste.jpg", "image", new byte[1]
        );

        mockMvc.perform(multipart("/obras/imagem")
                        .file(arquivoContentTypeInvalido)
                        .param("titulo", "Obra com Tipo Forjado"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Sucesso 3: Deve criar obra com título no limite máximo exato (50 caracteres)")
    void deveRetornar201_Sucesso3_TituloNoLimiteMaximo() throws Exception {
        when(storageService.upload(any())).thenReturn("https://cloudinary.com/mock.jpg");

        // Alterado de "arquivo" para "arquivos"
        MockMultipartFile arquivoValido = new MockMultipartFile(
                "arquivos", "teste.jpg", "image/jpeg", new byte[1]
        );

        // Atualizado para 50 caracteres
        String titulo50Chars = "A".repeat(50);

        mockMvc.perform(multipart("/obras/imagem")
                        .file(arquivoValido)
                        .param("titulo", titulo50Chars))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Erro 3: Deve bloquear upload retornando 400 quando título excede o limite (51 caracteres)")
    void deveRetornar400_Erro3_TituloAcimaDoLimite() throws Exception {
        // Alterado de "arquivo" para "arquivos"
        MockMultipartFile arquivoValido = new MockMultipartFile(
                "arquivos", "teste.jpg", "image/jpeg", new byte[1]
        );

        // Atualizado para 51 caracteres
        String tituloInvalido = "A".repeat(51);

        mockMvc.perform(multipart("/obras/imagem")
                        .file(arquivoValido)
                        .param("titulo", tituloInvalido))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @CsvSource({
            "true,  true,  true,  201",
            "false, true,  true,  403",
            "true,  false, true,  400",
            "true,  true,  false, 400"
    })
    void testarTabelaDeDecisao(boolean jwtValido, boolean arquivoPresente, boolean contentTypeImagem, int statusEsperado) throws Exception {

        if (!jwtValido) {
            SecurityContextHolder.clearContext();
        }

        when(storageService.upload(any())).thenReturn("https://cloudinary.com/mock.jpg");

        MockMultipartHttpServletRequestBuilder request = multipart("/obras/imagem");
        request.param("titulo", "Obra Teste");

        if (arquivoPresente) {
            String contentType = contentTypeImagem ? "image/jpeg" : "application/pdf";
            // Alterado de "arquivo" para "arquivos"
            MockMultipartFile arquivo = new MockMultipartFile("arquivos", "teste.jpg", contentType, "dados".getBytes());
            request.file(arquivo);
        }

        mockMvc.perform(request)
                .andExpect(status().is(statusEsperado));
    }

    // TESTES DE TRANSIÇÃO DE ESTADO (INTEGRAÇÃO)
    @Test
    @DisplayName("Transição Válida 1: Deve transitar de Inexistente -> Cadastrada -> Removida (Soft Delete)")
    void deveTransitarCicloDeVidaCompletoComSucesso() throws Exception {

        when(storageService.upload(any())).thenReturn("https://cloudinary.com/valida.jpg");
        // Alterado de "arquivo" para "arquivos"
        MockMultipartFile arquivo = new MockMultipartFile("arquivos", "arte.jpg", "image/jpeg", new byte[1]);

        String responseJson = mockMvc.perform(multipart("/obras/imagem")
                        .file(arquivo)
                        .param("titulo", "Obra Ciclo de Vida"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String obraId = responseJson.substring(responseJson.indexOf("\"id\":\"") + 6, responseJson.indexOf("\"", responseJson.indexOf("\"id\":\"") + 6));

        mockMvc.perform(delete("/obras/" + obraId))
                .andExpect(status().isNoContent());

        com.pessoal.galeria_ney.domain.Obra obraExcluida = repository.findById(UUID.fromString(obraId)).orElseThrow();
        Assertions.assertFalse(obraExcluida.isAtivo(), "A obra deveria estar com a flag ativo=false em vez de ser apagada.");
    }

    @Test
    @DisplayName("Transição Inválida 1: Deve falhar ao tentar excluir uma obra a partir de um estado inexistente")
    void deveBloquearExclusaoDeEstadoInexistente() throws Exception {

        String idInexistente = UUID.randomUUID().toString();

        mockMvc.perform(delete("/obras/" + idInexistente))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Transição Inválida 2: Deve bloquear transição de exclusão iniciada por usuário invasor (segurança)")
    void deveBloquearExclusaoPorUsuarioNaoAutorizado() throws Exception {

        Usuario autorVerdadeiro = new Usuario();
        autorVerdadeiro.setId(UUID.randomUUID());
        autorVerdadeiro.setRole(UserRole.USER);

        com.pessoal.galeria_ney.domain.Obra obraAlheia = com.pessoal.galeria_ney.domain.Obra.builder()
                .titulo("Obra de Terceiro")
                .autor(autorVerdadeiro)
                .tipo(TipoObra.IMAGEM)
                .urlMidia("https://site.com/foto.jpg")
                .ativo(true)
                .dataPostagem(LocalDate.now())
                .build();

        com.pessoal.galeria_ney.domain.Obra obraSalva = repository.save(obraAlheia);

        mockMvc.perform(delete("/obras/" + obraSalva.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Fluxograma de Integração: Deve interromper fluxo se o Cloudinary (Serviço Externo) falhar")
    void deveTratarFalhaNaIntegracaoExterna() throws Exception {

        // Alterado de "arquivo" para "arquivos"
        MockMultipartFile arquivoMock = new MockMultipartFile("arquivos", "teste.jpg", "image/jpeg", "dados".getBytes());

        when(storageService.upload(any())).thenThrow(new RuntimeException("Cloudinary fora do ar"));

        var requestBuilder = multipart("/obras/imagem")
                .file(arquivoMock)
                .param("titulo", "Obra Integrada");

        Exception excecao = Assertions.assertThrows(Exception.class, () -> {
            mockMvc.perform(requestBuilder);
        });

        Assertions.assertTrue(excecao.getCause() instanceof RuntimeException);
        Assertions.assertEquals("Cloudinary fora do ar", excecao.getCause().getMessage());
    }

    @Test
    @DisplayName("Deve bloquear o acesso e retornar 403 ao tentar deletar sem Token JWT")
    void deveDescobrirErroAcessoNaoAutenticado() throws Exception {
        SecurityContextHolder.clearContext();

        UUID idObra = UUID.randomUUID();

        mockMvc.perform(delete("/obras/{id}", idObra))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar cadastrar Vídeo sem URL")
    void deveDescobrirErroCadastroVideoSemUrl() throws Exception {

        String jsonRequisicao = """
            {
                "titulo": "Gameplay Minecraft",
                "tipo": "VIDEO_YOUTUBE",
                "descricao": "Melhores momentos"
            }
        """;

        mockMvc.perform(post("/obras")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(jsonRequisicao))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].campo").value("UrlMidia"))
                .andExpect(jsonPath("$[0].mensagem").value("Para videos e Musicas é preciso colocar o link!"));
    }
}