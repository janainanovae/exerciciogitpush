package com.exemplo.streaming;

import org.springframework.http.HttpStatus;

// Qualquer violação de regra de negócio. O status HTTP viaja junto com a exceção.
public class RegraDeNegocioException extends RuntimeException {

    private final HttpStatus status;

    public RegraDeNegocioException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static RegraDeNegocioException requisicaoInvalida(String mensagem) {
        return new RegraDeNegocioException(HttpStatus.BAD_REQUEST, mensagem);
    }

    public static RegraDeNegocioException naoAutenticado(String mensagem) {
        return new RegraDeNegocioException(HttpStatus.UNAUTHORIZED, mensagem);
    }

    public static RegraDeNegocioException acessoNegado(String mensagem) {
        return new RegraDeNegocioException(HttpStatus.FORBIDDEN, mensagem);
    }

    public static RegraDeNegocioException naoEncontrado(String mensagem) {
        return new RegraDeNegocioException(HttpStatus.NOT_FOUND, mensagem);
    }

    public static RegraDeNegocioException conflito(String mensagem) {
        return new RegraDeNegocioException(HttpStatus.CONFLICT, mensagem);
    }
}
