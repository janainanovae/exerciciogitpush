package com.exemplo.streaming;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ContaRepository contaRepository;
    private final RegistroHistoricoRepository historicoRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, ContaRepository contaRepository,
                          RegistroHistoricoRepository historicoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.contaRepository = contaRepository;
        this.historicoRepository = historicoRepository;
    }

    // Cria um usuário. O assinante abre uma conta nova; o não assinante entra em uma conta existente.
    // solicitanteId é quem está fazendo a requisição (só é exigido para criar não assinantes).
    @Transactional
    public Usuario criar(CriarUsuarioRequest dados, Long solicitanteId) {
        if (dados.tipo() == TipoUsuario.ASSINANTE) {
            return criarAssinante(dados);
        }
        return criarNaoAssinante(dados, solicitanteId);
    }

    private Usuario criarAssinante(CriarUsuarioRequest dados) {
        if (dados.plano() == null) {
            throw RegraDeNegocioException.requisicaoInvalida("O plano é obrigatório para criar um assinante.");
        }
        exigirEmailLivre(dados.email());

        Conta conta = contaRepository.save(new Conta(dados.plano()));

        // O assinante é o responsável pela conta, então o perfil dele é sempre ADULTO
        return usuarioRepository.save(
                new Usuario(dados.nome(), dados.email(), TipoUsuario.ASSINANTE, Perfil.ADULTO, conta));
    }

    private Usuario criarNaoAssinante(CriarUsuarioRequest dados, Long solicitanteId) {
        if (dados.contaId() == null || dados.perfil() == null) {
            throw RegraDeNegocioException.requisicaoInvalida(
                    "A conta (contaId) e o perfil são obrigatórios para criar um usuário não assinante.");
        }
        if (solicitanteId == null) {
            throw RegraDeNegocioException.naoAutenticado("Informe o usuário solicitante no cabeçalho X-Usuario-Id.");
        }

        // Controle de acesso: só o assinante da conta pode adicionar usuários
        Usuario solicitante = RegrasDeAcesso.buscarUsuario(usuarioRepository, solicitanteId);
        RegrasDeAcesso.exigirAssinanteDaConta(solicitante, dados.contaId());

        // Cota de compartilhamento do plano
        Conta conta = solicitante.getConta();
        long usados = usuarioRepository.countByContaIdAndTipo(conta.getId(), TipoUsuario.NAO_ASSINANTE);
        if (usados >= conta.getPlano().getCotaNaoAssinantes()) {
            throw RegraDeNegocioException.conflito("Cota de compartilhamento atingida: o plano " + conta.getPlano()
                    + " permite " + conta.getPlano().getCotaNaoAssinantes() + " usuário(s) não assinante(s).");
        }

        exigirEmailLivre(dados.email());

        return usuarioRepository.save(
                new Usuario(dados.nome(), dados.email(), TipoUsuario.NAO_ASSINANTE, dados.perfil(), conta));
    }

    // Remove um usuário não assinante. Só o assinante da conta pode fazer isso.
    @Transactional
    public void remover(Long usuarioId, Long solicitanteId) {
        Usuario alvo = RegrasDeAcesso.buscarUsuario(usuarioRepository, usuarioId);
        Usuario solicitante = RegrasDeAcesso.buscarUsuario(usuarioRepository, solicitanteId);

        RegrasDeAcesso.exigirAssinanteDaConta(solicitante, alvo.getConta().getId());

        if (alvo.isAssinante()) {
            throw RegraDeNegocioException.conflito("O assinante da conta não pode ser removido.");
        }

        // O histórico do usuário é apagado junto, para não deixar registros sem dono
        historicoRepository.deleteByUsuarioId(alvo.getId());
        usuarioRepository.delete(alvo);
    }

    private void exigirEmailLivre(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            throw RegraDeNegocioException.conflito("Já existe um usuário com esse e-mail.");
        }
    }
}
