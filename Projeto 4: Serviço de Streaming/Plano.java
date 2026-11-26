package com.exemplo.streaming;

// Planos em ordem crescente: um plano maior inclui o conteúdo dos menores.
// A cota é quantos usuários NÃO assinantes a conta pode ter (o assinante não conta).
public enum Plano {

    BASICO(1),
    PADRAO(2),
    PREMIUM(4);

    private final int cotaNaoAssinantes;

    Plano(int cotaNaoAssinantes) {
        this.cotaNaoAssinantes = cotaNaoAssinantes;
    }

    public int getCotaNaoAssinantes() {
        return cotaNaoAssinantes;
    }

    // Este plano dá acesso a um conteúdo que exige pelo menos o "planoMinimo"?
    public boolean permiteConteudoDoPlano(Plano planoMinimo) {
        return this.compareTo(planoMinimo) >= 0;
    }
}
