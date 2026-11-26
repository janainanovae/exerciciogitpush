package com.exemplo.streaming;

import java.time.LocalDateTime;

public record ItemHistoricoResponse(Long filmeId, String titulo, LocalDateTime assistidoEm) {

    public static ItemHistoricoResponse de(RegistroHistorico registro) {
        return new ItemHistoricoResponse(registro.getFilme().getId(), registro.getFilme().getTitulo(),
                registro.getAssistidoEm());
    }
}
