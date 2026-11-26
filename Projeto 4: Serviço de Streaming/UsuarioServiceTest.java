package com.exemplo.streaming;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private RegistroHistoricoRepository historicoRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    // ---------- Criação de usuários ----------

    @Test
    void criar_assinante_deveAbrirContaComOPlanoEPerfilAdulto() {
        when(usuarioRepository.existsByEmail("ana@email.com")).thenReturn(false);
        when(contaRepository.save(any(Conta.class))).thenAnswer(i -> i.getArgument(0));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        CriarUsuarioRequest dados = new CriarUsuarioRequest(
                "Ana", "ana@email.com", TipoUsuario.ASSINANTE, null, Plano.PADRAO, null);

        Usuario criado = usuarioService.criar(dados, null);

        assertThat(criado.getTipo()).isEqualTo(TipoUsuario.ASSINANTE);
        assertThat(criado.getPerfil()).isEqualTo(Perfil.ADULTO);
        assertThat(criado.getConta().getPlano()).isEqualTo(Plano.PADRAO);
    }

    @Test
    void criar_assinanteSemPlano_deveSerRecusado() {
        CriarUsuarioRequest dados = new CriarUsuarioRequest(
                "Ana", "ana@email.com", TipoUsuario.ASSINANTE, null, null, null);

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> usuarioService.criar(dados, null));

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(usuarioRepository, contaRepository);
    }

    @Test
    void criar_naoAssinante_deveEntrarNaContaDoAssinante() {
        Conta conta = Fabrica.conta(10L, Plano.PADRAO);
        Usuario assinante = Fabrica.usuario(1L, TipoUsuario.ASSINANTE, Perfil.ADULTO, conta);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(assinante));
        when(usuarioRepository.countByContaIdAndTipo(10L, TipoUsuario.NAO_ASSINANTE)).thenReturn(0L);
        when(usuarioRepository.existsByEmail("novo@email.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
        CriarUsuarioRequest dados = new CriarUsuarioRequest(
                "Novo", "novo@email.com", TipoUsuario.NAO_ASSINANTE, Perfil.ADOLESCENTE, null, 10L);

        Usuario criado = usuarioService.criar(dados, 1L);

        assertThat(criado.getTipo()).isEqualTo(TipoUsuario.NAO_ASSINANTE);
        assertThat(criado.getPerfil()).isEqualTo(Perfil.ADOLESCENTE);
        assertThat(criado.getConta()).isSameAs(conta);
    }

    // ---------- Restrição de acesso ao assinante ----------

    @Test
    void criar_naoAssinantePorQuemNaoEAssinante_deveSerNegado() {
        Conta conta = Fabrica.conta(10L, Plano.PREMIUM);
        Usuario filho = Fabrica.usuario(2L, TipoUsuario.NAO_ASSINANTE, Perfil.INFANTIL, conta);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(filho));
        CriarUsuarioRequest dados = new CriarUsuarioRequest(
                "Novo", "novo@email.com", TipoUsuario.NAO_ASSINANTE, Perfil.ADOLESCENTE, null, 10L);

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> usuarioService.criar(dados, 2L));

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void criar_naoAssinanteEmContaDeOutroAssinante_deveSerNegado() {
        Conta minhaConta = Fabrica.conta(10L, Plano.PREMIUM);
        Usuario assinante = Fabrica.usuario(1L, TipoUsuario.ASSINANTE, Perfil.ADULTO, minhaConta);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(assinante));
        // tenta adicionar na conta 99, que não é a dele
        CriarUsuarioRequest dados = new CriarUsuarioRequest(
                "Novo", "novo@email.com", TipoUsuario.NAO_ASSINANTE, Perfil.ADOLESCENTE, null, 99L);

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> usuarioService.criar(dados, 1L));

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(usuarioRepository, never()).save(any());
    }

    // ---------- Cota de compartilhamento ----------

    @Test
    void criar_naoAssinanteComCotaEsgotada_deveSerRecusado() {
        Conta conta = Fabrica.conta(10L, Plano.BASICO); // cota de 1 usuário não assinante
        Usuario assinante = Fabrica.usuario(1L, TipoUsuario.ASSINANTE, Perfil.ADULTO, conta);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(assinante));
        when(usuarioRepository.countByContaIdAndTipo(10L, TipoUsuario.NAO_ASSINANTE)).thenReturn(1L);
        CriarUsuarioRequest dados = new CriarUsuarioRequest(
                "Novo", "novo@email.com", TipoUsuario.NAO_ASSINANTE, Perfil.ADOLESCENTE, null, 10L);

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> usuarioService.criar(dados, 1L));

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(erro.getMessage()).contains("Cota");
        verify(usuarioRepository, never()).save(any());
    }

    // ---------- Remoção ----------

    @Test
    void remover_deveApagarOUsuarioENaoOHistoricoDeOutros() {
        Conta conta = Fabrica.conta(10L, Plano.PADRAO);
        Usuario assinante = Fabrica.usuario(1L, TipoUsuario.ASSINANTE, Perfil.ADULTO, conta);
        Usuario filho = Fabrica.usuario(2L, TipoUsuario.NAO_ASSINANTE, Perfil.INFANTIL, conta);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(filho));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(assinante));

        usuarioService.remover(2L, 1L);

        verify(historicoRepository).deleteByUsuarioId(2L);
        verify(usuarioRepository).delete(filho);
    }

    @Test
    void remover_porQuemNaoEAssinante_deveSerNegado() {
        Conta conta = Fabrica.conta(10L, Plano.PADRAO);
        Usuario filho = Fabrica.usuario(2L, TipoUsuario.NAO_ASSINANTE, Perfil.INFANTIL, conta);
        Usuario outroFilho = Fabrica.usuario(3L, TipoUsuario.NAO_ASSINANTE, Perfil.ADOLESCENTE, conta);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(filho));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(outroFilho));

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> usuarioService.remover(2L, 3L));

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(usuarioRepository, never()).delete(any());
    }

    @Test
    void remover_oProprioAssinante_deveSerRecusado() {
        Conta conta = Fabrica.conta(10L, Plano.PADRAO);
        Usuario assinante = Fabrica.usuario(1L, TipoUsuario.ASSINANTE, Perfil.ADULTO, conta);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(assinante));

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> usuarioService.remover(1L, 1L));

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        verify(usuarioRepository, never()).delete(any());
    }
}
