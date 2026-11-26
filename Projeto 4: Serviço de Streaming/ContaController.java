package com.exemplo.streaming;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    // PUT /contas/{id}/plano -> migra o plano de toda a conta (só o assinante)
    @PutMapping("/{id}/plano")
    public ContaResponse migrarPlano(@PathVariable Long id, @Valid @RequestBody MigrarPlanoRequest dados,
            @RequestHeader("X-Usuario-Id") Long solicitanteId) {
        return ContaResponse.de(contaService.migrarPlano(id, dados.plano(), solicitanteId));
    }

    // GET /contas/{id}/cota -> quanto da cota de compartilhamento já foi usada (só o assinante)
    @GetMapping("/{id}/cota")
    public CotaResponse cota(@PathVariable Long id, @RequestHeader("X-Usuario-Id") Long solicitanteId) {
        return contaService.consultarCota(id, solicitanteId);
    }
}
