package com.exemplo.streaming;

// Regras de controle de acesso compartilhadas pelos serviços
final class RegrasDeAcesso {

    private RegrasDeAcesso() {
    }

    static Usuario buscarUsuario(UsuarioRepository repository, Long id) {
        return repository.findById(id)
                .orElseThrow(() -> RegraDeNegocioException.naoEncontrado("Usuário não encontrado: " + id));
    }

    // Só o assinante DA CONTA informada pode continuar
    static void exigirAssinanteDaConta(Usuario solicitante, Long contaId) {
        boolean mesmaConta = solicitante.getConta().getId().equals(contaId);

        if (!solicitante.isAssinante() || !mesmaConta) {
            throw RegraDeNegocioException.acessoNegado("Apenas o assinante da conta pode realizar esta operação.");
        }
    }

    // O próprio usuário, ou o assinante da conta dele, pode continuar
    static void exigirProprioOuAssinanteDaConta(Usuario solicitante, Usuario alvo) {
        boolean proprio = solicitante.getId().equals(alvo.getId());
        boolean assinanteDaMesmaConta = solicitante.isAssinante()
                && solicitante.getConta().getId().equals(alvo.getConta().getId());

        if (!proprio && !assinanteDaMesmaConta) {
            throw RegraDeNegocioException.acessoNegado("Você não tem permissão para acessar os dados deste usuário.");
        }
    }
}
