package com.pessoal.galeria_ney.infra.utils;

import com.pessoal.galeria_ney.domain.TipoObra;
import com.pessoal.galeria_ney.infra.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmbedUrlResolverTest {

    // ==========================================
    // TESTES DO YOUTUBE
    // ==========================================

    @Test
    @DisplayName("Deve converter URL padrao do YouTube para formato de embed")
    void deveConverterYoutubePadrao() {
        String urlOriginal = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        String resultado = EmbedUrlResolver.getEmbedUrl(urlOriginal, TipoObra.VIDEO_YOUTUBE);

        assertEquals("https://www.youtube.com/embed/dQw4w9WgXcQ", resultado);
    }

    @Test
    @DisplayName("Deve limpar rastreadores e converter URL curta (youtu.be) do YouTube")
    void deveConverterYoutubeCurtoComRastreador() {
        String urlOriginal = "https://youtu.be/-iVgONy8kMY?si=ZfyXJJQfhecS4gMu";
        String resultado = EmbedUrlResolver.getEmbedUrl(urlOriginal, TipoObra.VIDEO_YOUTUBE);

        assertEquals("https://www.youtube.com/embed/-iVgONy8kMY", resultado);
    }

    @Test
    @DisplayName("Deve converter URL de Shorts do YouTube limpando parametros extras")
    void deveConverterYoutubeShorts() {
        String urlOriginal = "https://www.youtube.com/shorts/123456789?feature=share";
        String resultado = EmbedUrlResolver.getEmbedUrl(urlOriginal, TipoObra.VIDEO_YOUTUBE);

        assertEquals("https://www.youtube.com/embed/123456789", resultado);
    }

    @Test
    @DisplayName("Deve retornar a propria URL do YouTube intacta se ja for um link de embed")
    void deveRetornarEmbedYoutubeIntacto() {
        String urlOriginal = "https://www.youtube.com/embed/dQw4w9WgXcQ";
        String resultado = EmbedUrlResolver.getEmbedUrl(urlOriginal, TipoObra.VIDEO_YOUTUBE);

        assertEquals(urlOriginal, resultado);
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar converter URL invalida do YouTube")
    void deveLancarExcecaoYoutubeInvalido() {
        String urlInvalida = "https://www.youtube.com/um_link_qualquer_sem_id";

        assertThrows(RegraDeNegocioException.class, () -> {
            EmbedUrlResolver.getEmbedUrl(urlInvalida, TipoObra.VIDEO_YOUTUBE);
        });
    }

    // ==========================================
    // TESTES DO SPOTIFY
    // ==========================================

    @Test
    @DisplayName("Deve converter URL do Spotify e limpar rastreadores")
    void deveConverterSpotifyPadrao() {
        String urlOriginal = "https://open.spotify.com/intl-pt/track/2d64G7VaZdHQuAquz5HQNu?si=a7d6fb2a548443e7";
        String resultado = EmbedUrlResolver.getEmbedUrl(urlOriginal, TipoObra.AUDIO_SPOTIFY);

        assertEquals("https://open.spotify.com/embed/track/2d64G7VaZdHQuAquz5HQNu?utm_source=generator", resultado);
    }

    @Test
    @DisplayName("Deve retornar a propria URL do Spotify intacta se ja for um link de embed")
    void deveRetornarEmbedSpotifyIntacto() {
        String urlOriginal = "https://open.spotify.com/embed/track/2d64G7VaZdHQuAquz5HQNu?utm_source=generator";
        String resultado = EmbedUrlResolver.getEmbedUrl(urlOriginal, TipoObra.AUDIO_SPOTIFY);

        assertEquals(urlOriginal, resultado);
    }

    @Test
    @DisplayName("Deve lancar excecao ao tentar converter URL invalida do Spotify")
    void deveLancarExcecaoSpotifyInvalido() {
        String urlInvalida = "https://open.spotify.com/link_estranho_sem_id";

        assertThrows(RegraDeNegocioException.class, () -> {
            EmbedUrlResolver.getEmbedUrl(urlInvalida, TipoObra.AUDIO_SPOTIFY);
        });
    }

    // ==========================================
    // TESTES GERAIS (IMAGEM E NULOS)
    // ==========================================

    @Test
    @DisplayName("Nao deve alterar a URL se for uma IMAGEM")
    void naoDeveAlterarImagem() {
        String urlImagem = "https://res.cloudinary.com/minhafoto.jpg";
        String resultado = EmbedUrlResolver.getEmbedUrl(urlImagem, TipoObra.IMAGEM);

        assertEquals(urlImagem, resultado);
    }

    @Test
    @DisplayName("Deve lancar excecao se a URL recebida for vazia ou nula")
    void deveLancarExcecaoUrlVaziaOuNula() {
        assertThrows(RegraDeNegocioException.class, () -> {
            EmbedUrlResolver.getEmbedUrl("", TipoObra.VIDEO_YOUTUBE);
        });

        assertThrows(RegraDeNegocioException.class, () -> {
            EmbedUrlResolver.getEmbedUrl(null, TipoObra.AUDIO_SPOTIFY);
        });
    }
}