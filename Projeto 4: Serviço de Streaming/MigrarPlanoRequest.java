package com.exemplo.streaming;

import jakarta.validation.constraints.NotNull;

public record MigrarPlanoRequest(@NotNull(message = "O plano é obrigatório.") Plano plano) {
}
