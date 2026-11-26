package com.exemplo.streaming;

// Situação da cota de compartilhamento da conta (só conta usuários não assinantes)
public record CotaResponse(Plano plano, int cotaTotal, int cotaUsada, int cotaDisponivel) {
}
