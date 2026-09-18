package br.com.smartcollector.api.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(recurso + " " + id + " nao encontrado.");
    }

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
