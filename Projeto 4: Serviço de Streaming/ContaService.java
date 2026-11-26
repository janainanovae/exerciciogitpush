package com.exemplo.streaming;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContaService {

    private final UsuarioRepository usuarioRepository;
    private final ContaRepository contaRepository;

    public ContaService(UsuarioRepository usuarioRepository, ContaRepository contaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.contaRepository = contaRepository;
    }

    // Migra o plano da conta. Como o plano pertence à conta, a mudança vale para todos os usuários dela.
    @Transactional
    public Conta migrarPlano(Long contaId, Plano novoPlano, Long solicitanteId) {
        Usuario solicitante = RegrasDeAcesso.buscarUsuario(usuarioRepository, solicitanteId);
        RegrasDeAcesso.exigirAssinanteDaConta(solicitante, contaId);

        Conta conta = solicitante.getConta();

        // Não deixa migrar para um plano cuja cota é menor que o número de usuários já cadastrados
        long usados = usuarioRepository.countByContaIdAndTipo(conta.getId(), TipoUsuario.NAO_ASSINANTE);
        if (usados > novoPlano.getCotaNaoAssinantes()) {
            throw RegraDeNegocioException.conflito("O plano " + novoPlano + " permite apenas "
                    + novoPlano.getCotaNaoAssinantes() + " usuário(s) não assinante(s), mas a conta tem " + usados
                    + ". Remova " + (usados - novoPlano.getCotaNaoAssinantes()) + " usuário(s) antes de migrar.");
        }

        conta.setPlano(novoPlano);
        return contaRepository.save(conta);
    }

    @Transactional(readOnly = true)
    public CotaResponse consultarCota(Long contaId, Long solicitanteId) {
        Usuario solicitante = RegrasDeAcesso.buscarUsuario(usuarioRepository, solicitanteId);
        RegrasDeAcesso.exigirAssinanteDaConta(solicitante, contaId);

        Conta conta = solicitante.getConta();
        int total = conta.getPlano().getCotaNaoAssinantes();
        int usada = (int) usuarioRepository.countByContaIdAndTipo(conta.getId(), TipoUsuario.NAO_ASSINANTE);

        return new CotaResponse(conta.getPlano(), total, usada, total - usada);
    }
}
