package com.pessoal.galeria_ney.infra.utils;

import com.pessoal.galeria_ney.domain.TipoObra;
import com.pessoal.galeria_ney.infra.exception.RegraDeNegocioException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmbedUrlResolver {

    public static String getEmbedUrl(String url, TipoObra tipo) {
        if (url == null || url.isBlank()) return url;
        if (tipo == TipoObra.IMAGEM) return url;
        if (tipo == TipoObra.VIDEO_YOUTUBE) return resolveYouTube(url);
        if (tipo == TipoObra.AUDIO_SPOTIFY) return resolveSpotify(url);
        return url;
    }

    private static String resolveYouTube(String url) {
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return "https://www.youtube.com/embed/" + matcher.group();
        }
        throw new RegraDeNegocioException("urlMidia", "URL do YouTube em formato inválido.");
    }

    private static String resolveSpotify(String url) {
        if (url.contains("track/")) {
            String[] parts = url.split("track/");
            String id = parts[1].split("\\?")[0];
            return "https://open.spotify.com/embed/track/" + id;
        }
        throw new RegraDeNegocioException("urlMidia", "Apenas links de faixas (tracks) do Spotify são permitidos.");
    }
}