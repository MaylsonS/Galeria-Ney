package com.pessoal.galeria_ney.service;

import com.pessoal.galeria_ney.domain.Obra;
import com.pessoal.galeria_ney.domain.TipoObra;
import com.pessoal.galeria_ney.infra.exception.RegraDeNegocioException;
import com.pessoal.galeria_ney.infra.utils.EmbedUrlResolver;
import com.pessoal.galeria_ney.repository.ObraRepository;
import com.pessoal.galeria_ney.service.storage.MidiaStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class ObraService {

    private final ObraRepository repository;
    private final MidiaStorageService storageService;
    public ObraService(ObraRepository repository,  MidiaStorageService storageService) {
        this.repository = repository;
        this.storageService = storageService;
    }


    public List<Obra> listar(String termo){
        if(termo==null || termo.isEmpty()){
            return  repository.findAll();
        }
        return repository.findByTituloContainingIgnoreCase(termo);
    }

    public Obra cadastrar(Obra obra) {
        validarUrlMidia(obra);
        return repository.save(obra);
    }

    public Obra cadatrarImagem(MultipartFile arquivo, Obra obra) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraDeNegocioException("arquivo", "O arquivo da imagem é obrigatório.");
        }

        String urlImagem = storageService.upload(arquivo);
        obra.setUrlMidia(urlImagem);
        obra.setTipo(TipoObra.IMAGEM);

        return repository.save(obra);
    }

    public Obra buscarPorId(UUID id) {
        return  repository.findById(id).orElseThrow(()-> new RegraDeNegocioException("id","Obra não encontrada"));
    }

    public Obra atualizar(UUID id,Obra obraAlterada) {

        Obra obraAntiga = buscarPorId(id);

        obraAntiga.setTitulo(obraAlterada.getTitulo());
        obraAntiga.setDescricao(obraAlterada.getDescricao());
        obraAntiga.setTipo(obraAlterada.getTipo());
        obraAntiga.setUrlMidia(obraAlterada.getUrlMidia());

        validarUrlMidia(obraAntiga);

        return repository.save(obraAntiga);
    }

    public void excluir(UUID id) {
        Obra obraEncontrada = buscarPorId(id);
        repository.delete(obraEncontrada);
    }

    public void validarUrlMidia(Obra obra) {
        if (obra.getTipo() != TipoObra.IMAGEM) {
            if (obra.getUrlMidia() == null || obra.getUrlMidia().isBlank()) {
                throw new RegraDeNegocioException("UrlMidia", "Para videos e Musicas é preciso colcoar o link !");
            }

            EmbedUrlResolver.getEmbedUrl(obra.getUrlMidia(), obra.getTipo());
        }
    }
}