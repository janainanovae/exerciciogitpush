package com.exemplo.streaming;

// Perfis em ordem crescente de maturidade: um perfil maior pode assistir ao conteúdo dos menores.
public enum Perfil {

    INFANTIL,
    ADOLESCENTE,
    ADULTO;

    // Este perfil pode assistir a um filme que exige pelo menos o perfil "perfilMinimo"?
    public boolean podeAssistir(Perfil perfilMinimo) {
        return this.compareTo(perfilMinimo) >= 0;
    }
}
