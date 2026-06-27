package com.pessoal.galeria_ney.infra.utils;

import com.pessoal.galeria_ney.domain.TipoObra;
import com.pessoal.galeria_ney.infra.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmbedUrlResolverTest {

    @Test
    @DisplayName("Deve converter URL padrao do YouTube para formato de embed")
    void deveConverterYoutubePadrao() {
        String urlOriginal = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        String resultado = EmbedUrlResolver.getEmbedUrl(urlOriginal, TipoObra.VIDEO_YOUTUBE);

        assertEquals("https://www.youtube.com/embed/dQw4w9WgXcQ", resultado);
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar converter URL invalida do YouTube")
    void deveLancarExcecaoYoutubeInvalido() {
        String urlInvalida = "https://www.youtube.com/um_link_qualquer_sem_id";

        assertThrows(RegraDeNegocioException.class, () -> {
            EmbedUrlResolver.getEmbedUrl(urlInvalida, TipoObra.VIDEO_YOUTUBE);
        });
    }

    @Test
    @DisplayName("Nao deve alterar a URL se for uma IMAGEM")
    void naoDeveAlterarImagem() {
        String urlImagem = "https://res.cloudinary.com/minhafoto.jpg";
        String resultado = EmbedUrlResolver.getEmbedUrl(urlImagem, TipoObra.IMAGEM);

        assertEquals(urlImagem, resultado);
    }
}