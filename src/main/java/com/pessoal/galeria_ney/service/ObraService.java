package com.pessoal.galeria_ney.service;

import com.pessoal.galeria_ney.domain.Obra;
import com.pessoal.galeria_ney.domain.TipoObra;
import com.pessoal.galeria_ney.domain.UserRole;
import com.pessoal.galeria_ney.domain.Usuario;
import com.pessoal.galeria_ney.infra.exception.RegraDeNegocioException;
import com.pessoal.galeria_ney.infra.utils.EmbedUrlResolver;
import com.pessoal.galeria_ney.repository.ObraRepository;
import com.pessoal.galeria_ney.service.storage.MidiaStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class ObraService {

    private final ObraRepository repository;
    private final MidiaStorageService storageService;

    public ObraService(ObraRepository repository, MidiaStorageService storageService) {
        this.repository = repository;
        this.storageService = storageService;
    }

    private Usuario getUsuarioLogado() {
        return (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void validarDadosBasicos(Obra obra) {
        if (obra.getTitulo() == null || obra.getTitulo().trim().isEmpty()) {
            throw new RegraDeNegocioException("titulo", "O titulo é obrigatorio");
        }
        if (obra.getTitulo().length() > 50) {
            throw new RegraDeNegocioException("titulo", "O título deve ter no máximo 50 caracteres");
        }
    }

    private void verificarPermissao(Obra obra) {
        Usuario usuarioLogado = getUsuarioLogado();

        if (usuarioLogado.getRole() == UserRole.ADMIN) {
            return;
        }

        if (!obra.getAutor().getId().equals(usuarioLogado.getId())) {
            throw new RegraDeNegocioException("permissao", "Acesso Negado: Você só pode editar ou excluir as suas próprias obras.");
        }
    }

    public Page<Obra> listar(String termo, Pageable pageable) {
        if(termo == null || termo.isEmpty()){
            return repository.findByAtivoTrue(pageable);
        }
        return repository.findByTituloContainingIgnoreCaseAndAtivoTrue(termo, pageable);
    }

    public Page<Obra> listarPorAutor(UUID autorId, Pageable pageable) {
        return repository.findByAutorIdAndAtivoTrue(autorId, pageable);
    }

    public Page<Obra> listarPorAutorFiltrado(UUID autorId, TipoObra tipo, String termo, Pageable pageable) {
        boolean temTipo = tipo != null;
        boolean temTermo = termo != null && !termo.trim().isEmpty();

        // Remove espaços acidentais nas bordas do texto digitado
        String termoLimpo = temTermo ? termo.trim() : null;

        if (temTipo && temTermo) {
            return repository.findByAutorIdAndTipoAndTituloContainingIgnoreCaseAndAtivoTrue(autorId, tipo, termoLimpo, pageable);
        } else if (temTipo) {
            return repository.findByAutorIdAndTipoAndAtivoTrue(autorId, tipo, pageable);
        } else if (temTermo) {
            return repository.findByAutorIdAndTituloContainingIgnoreCaseAndAtivoTrue(autorId, termoLimpo, pageable);
        } else {
            return repository.findByAutorIdAndAtivoTrue(autorId, pageable);
        }
    }

    public Obra cadastrar(Obra obra) {
        validarDadosBasicos(obra);
        validarUrlMidia(obra);
        obra.setAutor(getUsuarioLogado());
        obra.setAtivo(true);
        return repository.save(obra);
    }

    public Obra cadastrarImagem(java.util.List<MultipartFile> arquivos, Obra obra) {
        if (arquivos == null || arquivos.isEmpty()) {
            throw new RegraDeNegocioException("arquivos", "É obrigatório enviar pelo menos um arquivo de mídia.");
        }

        validarDadosBasicos(obra);

        java.util.List<String> urlsGeradas = new java.util.ArrayList<>();

        for (MultipartFile arquivo : arquivos) {
            if (arquivo.getContentType() == null || (!arquivo.getContentType().startsWith("image/") && !arquivo.getContentType().startsWith("video/"))) {
                throw new RegraDeNegocioException("arquivos", "O arquivo enviado não é uma mídia válida.");
            }
            urlsGeradas.add(storageService.upload(arquivo));
        }

        obra.setUrlMidia(urlsGeradas.get(0));

        // Se houverem mais arquivos, tira o primeiro e salva o restante na nova lista de mídias extras
        if (urlsGeradas.size() > 1) {
            urlsGeradas.remove(0);
            obra.setMidias(urlsGeradas);
        }

        obra.setTipo(TipoObra.IMAGEM);
        obra.setAutor(getUsuarioLogado());
        obra.setAtivo(true);

        return repository.save(obra);
    }

    public Obra buscarPorId(UUID id) {
        Obra obra = repository.findById(id).orElseThrow(() -> new RegraDeNegocioException("id","Obra não encontrada"));
        if(!obra.isAtivo()) {
            throw new RegraDeNegocioException("id", "Esta obra foi excluída e não está mais disponível.");
        }
        return obra;
    }

    public Obra atualizar(UUID id, Obra obraAlterada) {
        Obra obraAntiga = buscarPorId(id);

        validarDadosBasicos(obraAlterada);
        verificarPermissao(obraAntiga);

        obraAntiga.setTitulo(obraAlterada.getTitulo());
        obraAntiga.setDescricao(obraAlterada.getDescricao());
        obraAntiga.setTipo(obraAlterada.getTipo());
        obraAntiga.setUrlMidia(obraAlterada.getUrlMidia());

        validarUrlMidia(obraAntiga);

        return repository.save(obraAntiga);
    }

    public void excluir(UUID id) {
        Obra obraEncontrada = buscarPorId(id);
        verificarPermissao(obraEncontrada);

        obraEncontrada.setAtivo(false);
        repository.save(obraEncontrada);
    }

    public void validarUrlMidia(Obra obra) {
        if (obra.getTipo() != TipoObra.IMAGEM) {
            if (obra.getUrlMidia() == null || obra.getUrlMidia().isBlank()) {
                throw new RegraDeNegocioException("UrlMidia", "Para videos e Musicas é preciso colocar o link!");
            }
            String urlConvertida = EmbedUrlResolver.getEmbedUrl(obra.getUrlMidia(), obra.getTipo());
            obra.setUrlMidia(urlConvertida);
        }
    }
}