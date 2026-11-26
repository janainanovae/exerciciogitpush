package com.exemplo.streaming;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

// O usuário que faz a requisição é identificado pelo cabeçalho X-Usuario-Id
// (simplificação: em um sistema real, viria da autenticação/sessão).
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final FilmeService filmeService;

    public UsuarioController(UsuarioService usuarioService, FilmeService filmeService) {
        this.usuarioService = usuarioService;
        this.filmeService = filmeService;
    }

    // POST /usuarios -> cria assinante (conta nova) ou não assinante (precisa do cabeçalho do assinante)
    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody CriarUsuarioRequest dados,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long solicitanteId) {
        Usuario criado = usuarioService.criar(dados, solicitanteId);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.de(criado));
    }

    // DELETE /usuarios/{id} -> remove um usuário não assinante (só o assinante da conta)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id, @RequestHeader("X-Usuario-Id") Long solicitanteId) {
        usuarioService.remover(id, solicitanteId);
        return ResponseEntity.noContent().build();
    }

    // GET /usuarios/{id}/historico -> filmes assistidos, na ordem em que foram assistidos
    @GetMapping("/{id}/historico")
    public List<ItemHistoricoResponse> historico(@PathVariable Long id,
            @RequestHeader("X-Usuario-Id") Long solicitanteId) {
        return filmeService.historico(id, solicitanteId).stream()
                .map(ItemHistoricoResponse::de)
                .toList();
    }
}
