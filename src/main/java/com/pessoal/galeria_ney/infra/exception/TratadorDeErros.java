package com.pessoal.galeria_ney.infra.exception;


import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<DadosErroValidacao>> tratarErro400(MethodArgumentNotValidException ex) {
        var erros = ex.getFieldErrors();

        return ResponseEntity.badRequest().body(erros.stream()
                .map(DadosErroValidacao::new)
                .toList());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<List<DadosErroValidacao>> tratarErroRegraDeNegocio(RegraDeNegocioException ex) {
        var erro = new DadosErroValidacao(ex.getCampo(), ex.getMessage());
        return ResponseEntity.badRequest().body(List.of(erro));
    }

    public record DadosErroValidacao(String campo, String mensagem) {
        public DadosErroValidacao(FieldError erro) {
            this(erro.getField(), erro.getDefaultMessage());
        }
    }



}
