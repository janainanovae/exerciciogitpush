package com.exemplo.streaming;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Corpo da criação de usuário.
// ASSINANTE: informar plano (cria uma conta nova). NAO_ASSINANTE: informar contaId e perfil.
public record CriarUsuarioRequest(
        @NotBlank(message = "O nome é obrigatório.") String nome,
        @NotBlank(message = "O e-mail é obrigatório.") String email,
        @NotNull(message = "O tipo (ASSINANTE ou NAO_ASSINANTE) é obrigatório.") TipoUsuario tipo,
        Perfil perfil,
        Plano plano,
        Long contaId) {
}
