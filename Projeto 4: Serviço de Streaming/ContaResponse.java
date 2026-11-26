package com.exemplo.streaming;

public record ContaResponse(Long id, Plano plano) {

    public static ContaResponse de(Conta conta) {
        return new ContaResponse(conta.getId(), conta.getPlano());
    }
}
