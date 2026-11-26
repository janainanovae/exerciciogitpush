package com.exemplo.streaming;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FilmeService {

    private final FilmeRepository filmeRepository;
    private final UsuarioRepository usuarioRepository;
    private final RegistroHistoricoRepository historicoRepository;

    public FilmeService(FilmeRepository filmeRepository, UsuarioRepository usuarioRepository,
                        RegistroHistoricoRepository historicoRepository) {
        this.filmeRepository = filmeRepository;
        this.usuarioRepository = usuarioRepository;
        this.historicoRepository = historicoRepository;
    }

    // Filmes que o usuário pode ver: o plano da conta precisa incluir o filme E o perfil precisa permitir
    @Transactional(readOnly = true)
    public List<Filme> listarDisponiveis(Long solicitanteId) {
        Usuario usuario = RegrasDeAcesso.buscarUsuario(usuarioRepository, solicitanteId);

        return filmeRepository.findAll().stream()
                .filter(filme -> planoPermite(usuario, filme) && perfilPermite(usuario, filme))
                .sorted(Comparator.comparing(Filme::getTitulo))
                .toList();
    }

    // Assiste a um filme: bloqueia se o perfil ou o plano não permitirem; se permitir, salva no histórico
    @Transactional
    public RegistroHistorico assistir(Long filmeId, Long solicitanteId) {
        Usuario usuario = RegrasDeAcesso.buscarUsuario(usuarioRepository, solicitanteId);
        Filme filme = filmeRepository.findById(filmeId)
                .orElseThrow(() -> RegraDeNegocioException.naoEncontrado("Filme não encontrado: " + filmeId));

        if (!perfilPermite(usuario, filme)) {
            throw RegraDeNegocioException.acessoNegado("O perfil " + usuario.getPerfil()
                    + " não permite assistir a este filme (perfil mínimo: " + filme.getPerfilMinimo() + ").");
        }
        if (!planoPermite(usuario, filme)) {
            throw RegraDeNegocioException.acessoNegado("O plano " + usuario.getConta().getPlano()
                    + " da conta não inclui este filme (plano mínimo: " + filme.getPlanoMinimo() + ").");
        }

        return historicoRepository.save(new RegistroHistorico(usuario, filme));
    }

    // Histórico na ordem em que os filmes foram assistidos. Repetições aparecem de novo, cada uma na sua vez.
    // Pode ser consultado pelo próprio usuário ou pelo assinante da conta dele.
    @Transactional(readOnly = true)
    public List<RegistroHistorico> historico(Long usuarioId, Long solicitanteId) {
        Usuario alvo = RegrasDeAcesso.buscarUsuario(usuarioRepository, usuarioId);
        Usuario solicitante = RegrasDeAcesso.buscarUsuario(usuarioRepository, solicitanteId);

        RegrasDeAcesso.exigirProprioOuAssinanteDaConta(solicitante, alvo);

        return historicoRepository.findByUsuarioIdOrderByIdAsc(usuarioId);
    }

    private boolean planoPermite(Usuario usuario, Filme filme) {
        return usuario.getConta().getPlano().permiteConteudoDoPlano(filme.getPlanoMinimo());
    }

    private boolean perfilPermite(Usuario usuario, Filme filme) {
        return usuario.getPerfil().podeAssistir(filme.getPerfilMinimo());
    }
}
