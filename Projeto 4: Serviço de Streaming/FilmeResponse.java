package com.exemplo.streaming;

public record FilmeResponse(Long id, String titulo, Perfil perfilMinimo, Plano planoMinimo) {

    public static FilmeResponse de(Filme filme) {
        return new FilmeResponse(filme.getId(), filme.getTitulo(), filme.getPerfilMinimo(), filme.getPlanoMinimo());
    }
}
