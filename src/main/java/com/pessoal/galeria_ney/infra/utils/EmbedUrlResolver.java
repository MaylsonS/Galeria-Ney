package com.pessoal.galeria_ney.infra.utils;

import com.pessoal.galeria_ney.domain.TipoObra;
import com.pessoal.galeria_ney.infra.exception.RegraDeNegocioException;

public class EmbedUrlResolver {

    public static String getEmbedUrl(String url, TipoObra tipo) {
        if (url == null || url.isBlank()) {
            throw new RegraDeNegocioException("UrlMidia", "A URL não pode estar vazia.");
        }

        if (tipo == TipoObra.VIDEO_YOUTUBE) {
            return resolverYouTube(url);
        } else if (tipo == TipoObra.AUDIO_SPOTIFY) {
            return resolverSpotify(url);
        }

        return url; // Se for imagem, retorna a url original do Cloudinary
    }

    private static String resolverYouTube(String url) {
        // Trava de segurança: Se já for um link de embed salvo no banco, retorna ele mesmo intacto!
        if (url.contains("youtube.com/embed/")) {
            return url;
        }

        String videoId = null;

        // 1. YouTube Shorts
        if (url.contains("/shorts/")) {
            videoId = url.substring(url.indexOf("/shorts/") + 8);
        }
        // 2. Link Padrão
        else if (url.contains("watch?v=")) {
            videoId = url.substring(url.indexOf("watch?v=") + 8);
        }
        // 3. Link Curto
        else if (url.contains("youtu.be/")) {
            videoId = url.substring(url.indexOf("youtu.be/") + 9);
        }

        if (videoId != null) {
            // Limpa rastreadores
            videoId = videoId.split("\\?")[0];
            videoId = videoId.split("&")[0];

            return "https://www.youtube.com/embed/" + videoId;
        }

        throw new RegraDeNegocioException("UrlMidia", "URL do YouTube em formato inválido. Use links padrão, youtu.be ou shorts.");
    }

    private static String resolverSpotify(String url) {
        // Trava de segurança: Se já for um link de embed salvo no banco, retorna ele mesmo intacto!
        if (url.contains("spotify.com/embed/")) {
            return url;
        }

        if (url.contains("spotify.com/")) {
            String path = url.substring(url.indexOf("spotify.com/") + 12);
            path = path.split("\\?")[0];
            String[] parts = path.split("/");

            if (parts.length >= 2) {
                String tipoMidia = parts[parts.length - 2];
                String id = parts[parts.length - 1];

                return "https://open.spotify.com/embed/" + tipoMidia + "/" + id + "?utm_source=generator";
            }
        }

        throw new RegraDeNegocioException("UrlMidia", "URL do Spotify em formato inválido. Copie o link direto da música.");
    }
}