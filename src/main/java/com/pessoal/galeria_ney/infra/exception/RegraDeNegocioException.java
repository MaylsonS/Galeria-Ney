package com.pessoal.galeria_ney.infra.exception;

import lombok.Getter;

@Getter
public class RegraDeNegocioException extends RuntimeException {
    private final String campo;

    public RegraDeNegocioException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }
}
