package com.pessoal.galeria_ney.controller;

import com.pessoal.galeria_ney.domain.UserRole;
import com.pessoal.galeria_ney.domain.Usuario;
import com.pessoal.galeria_ney.repository.ObraRepository;
import com.pessoal.galeria_ney.service.storage.MidiaStorageService;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

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

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(usuarioMock, null, usuarioMock.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }


    @Test
    @DisplayName("Sucesso 1: Deve criar obra com arquivo e título nos limites mínimos (1 byte / 1 char)")
    void deveRetornar201_Sucesso1_EnvioMultipartNoLimiteMinimo() throws Exception {
        when(storageService.upload(any())).thenReturn("https://cloudinary.com/mock.jpg");

        MockMultipartFile arquivo1Byte = new MockMultipartFile(
                "arquivo", "teste.jpg", "image/jpeg", new byte[1]
        );

        mockMvc.perform(multipart("/obras/imagem")
                        .file(arquivo1Byte)
                        .param("titulo", "A"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Sucesso 2: Deve retornar 200 OK acionando repository.findAll() quando termo for vazio (tamanho 0)")
    void deveRetornar200_Sucesso2_BuscaNoLimiteVazio() throws Exception {
        mockMvc.perform(get("/obras")
                        .param("termo", ""))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Erro 1: Deve bloquear upload retornando 400 quando o arquivo enviado for vazio (0 bytes)")
    void deveRetornar400_Erro1_AusenciaDePayloadNoLimite() throws Exception {
        MockMultipartFile arquivoVazio = new MockMultipartFile(
                "arquivo", "teste.jpg", "image/jpeg", new byte[0]
        );

        mockMvc.perform(multipart("/obras/imagem")
                        .file(arquivoVazio)
                        .param("titulo", "Obra Sem Foto"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Erro 2: Deve bloquear upload retornando 400 na ausência da barra requerida no Content-Type")
    void deveRetornar400_Erro2_LimiteDaRegraDeContentType() throws Exception {
        MockMultipartFile arquivoContentTypeInvalido = new MockMultipartFile(
                "arquivo", "teste.jpg", "image", new byte[1]
        );

        mockMvc.perform(multipart("/obras/imagem")
                        .file(arquivoContentTypeInvalido)
                        .param("titulo", "Obra com Tipo Forjado"))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("Sucesso 3: Deve criar obra com título no limite máximo exato (20 caracteres)")
    void deveRetornar201_Sucesso3_TituloNoLimiteMaximo() throws Exception {
        when(storageService.upload(any())).thenReturn("https://cloudinary.com/mock.jpg");

        MockMultipartFile arquivoValido = new MockMultipartFile(
                "arquivo", "teste.jpg", "image/jpeg", new byte[1]
        );

        String titulo20Chars = "A".repeat(20);

        mockMvc.perform(multipart("/obras/imagem")
                        .file(arquivoValido)
                        .param("titulo", titulo20Chars))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Erro 3: Deve bloquear upload retornando 400 quando título excede o limite (21 caracteres)")
    void deveRetornar400_Erro3_TituloAcimaDoLimite() throws Exception {
        MockMultipartFile arquivoValido = new MockMultipartFile(
                "arquivo", "teste.jpg", "image/jpeg", new byte[1]
        );

        String tituloInvalido = "A".repeat(21);

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

        MockMultipartHttpServletRequestBuilder request = multipart("/obras/imagem");
        request.param("titulo", "Obra Teste");

        if (arquivoPresente) {
            String contentType = contentTypeImagem ? "image/jpeg" : "application/pdf";
            MockMultipartFile arquivo = new MockMultipartFile("arquivo", "teste.jpg", contentType, "dados".getBytes());
            request.file(arquivo);
        }


        mockMvc.perform(request)
                .andExpect(status().is(statusEsperado));
    }


    // TESTES DE TRANSIÇÃO DE ESTADO (INTEGRAÇÃO)
    @Test
    @DisplayName("Transição Válida 1: Deve transitar de Inexistente -> Cadastrada -> Removida")
    void deveTransitarCicloDeVidaCompletoComSucesso() throws Exception {

        when(storageService.upload(any())).thenReturn("https://cloudinary.com/valida.jpg");
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "arte.jpg", "image/jpeg", new byte[1]);

        String responseJson = mockMvc.perform(multipart("/obras/imagem")
                        .file(arquivo)
                        .param("titulo", "Obra Ciclo de Vida"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String obraId = responseJson.substring(responseJson.indexOf("\"id\":\"") + 6, responseJson.indexOf("\"", responseJson.indexOf("\"id\":\"") + 6));

        mockMvc.perform(delete("/obras/" + obraId))
                .andExpect(status().isNoContent());
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
                .tipo(com.pessoal.galeria_ney.domain.TipoObra.IMAGEM)
                .urlMidia("https://site.com/foto.jpg")
                .build();

        com.pessoal.galeria_ney.domain.Obra obraSalva = repository.save(obraAlheia);

        mockMvc.perform(delete("/obras/" + obraSalva.getId()))
                .andExpect(status().isBadRequest());
    }


}