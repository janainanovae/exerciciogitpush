package com.exemplo.streaming;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/filmes")
public class FilmeController {

    private final FilmeService filmeService;

    public FilmeController(FilmeService filmeService) {
        this.filmeService = filmeService;
    }

    // GET /filmes -> filmes disponíveis para o plano e o perfil de quem faz a requisição
    @GetMapping
    public List<FilmeResponse> listar(@RequestHeader("X-Usuario-Id") Long solicitanteId) {
        return filmeService.listarDisponiveis(solicitanteId).stream()
                .map(FilmeResponse::de)
                .toList();
    }

    // POST /filmes/{id}/assistir -> registra no histórico, ou bloqueia (403) se o perfil/plano não permitir
    @PostMapping("/{id}/assistir")
    public ResponseEntity<ItemHistoricoResponse> assistir(@PathVariable Long id,
            @RequestHeader("X-Usuario-Id") Long solicitanteId) {
        RegistroHistorico registro = filmeService.assistir(id, solicitanteId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ItemHistoricoResponse.de(registro));
    }
}
