package com.exemplo.streaming;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class FilmeServiceTest {

    @Mock
    private FilmeRepository filmeRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RegistroHistoricoRepository historicoRepository;

    @InjectMocks
    private FilmeService filmeService;

    // ---------- Listagem por plano e perfil ----------

    @Test
    void listarDisponiveis_criancaNoPlanoBasico_veSoFilmesInfantisDoPlanoBasico() {
        Conta conta = Fabrica.conta(10L, Plano.BASICO);
        Usuario crianca = Fabrica.usuario(2L, TipoUsuario.NAO_ASSINANTE, Perfil.INFANTIL, conta);
        Filme infantilBasico = Fabrica.filme(1L, "Infantil Basico", Perfil.INFANTIL, Plano.BASICO);
        Filme adolescenteBasico = Fabrica.filme(2L, "Adolescente Basico", Perfil.ADOLESCENTE, Plano.BASICO);
        Filme infantilPremium = Fabrica.filme(3L, "Infantil Premium", Perfil.INFANTIL, Plano.PREMIUM);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(crianca));
        when(filmeRepository.findAll()).thenReturn(List.of(infantilBasico, adolescenteBasico, infantilPremium));

        List<Filme> disponiveis = filmeService.listarDisponiveis(2L);

        assertThat(disponiveis).containsExactly(infantilBasico);
    }

    @Test
    void listarDisponiveis_adultoNoPlanoPremium_veTudoOrdenadoPorTitulo() {
        Conta conta = Fabrica.conta(10L, Plano.PREMIUM);
        Usuario adulto = Fabrica.usuario(1L, TipoUsuario.ASSINANTE, Perfil.ADULTO, conta);
        Filme zebra = Fabrica.filme(1L, "Zebra", Perfil.ADULTO, Plano.PREMIUM);
        Filme alfa = Fabrica.filme(2L, "Alfa", Perfil.INFANTIL, Plano.BASICO);
        Filme meio = Fabrica.filme(3L, "Meio", Perfil.ADOLESCENTE, Plano.PADRAO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adulto));
        when(filmeRepository.findAll()).thenReturn(List.of(zebra, alfa, meio));

        List<Filme> disponiveis = filmeService.listarDisponiveis(1L);

        assertThat(disponiveis).containsExactly(alfa, meio, zebra);
    }

    // ---------- Assistir: bloqueio por perfil e por plano ----------

    @Test
    void assistir_perfilNaoPermitido_deveBloquearENaoSalvarNoHistorico() {
        Conta conta = Fabrica.conta(10L, Plano.PREMIUM);
        Usuario crianca = Fabrica.usuario(2L, TipoUsuario.NAO_ASSINANTE, Perfil.INFANTIL, conta);
        Filme filmeAdulto = Fabrica.filme(5L, "Filme Adulto", Perfil.ADULTO, Plano.BASICO);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(crianca));
        when(filmeRepository.findById(5L)).thenReturn(Optional.of(filmeAdulto));

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> filmeService.assistir(5L, 2L));

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(erro.getMessage()).contains("perfil");
        verify(historicoRepository, never()).save(any());
    }

    @Test
    void assistir_planoNaoIncluiOFilme_deveBloquearENaoSalvarNoHistorico() {
        Conta conta = Fabrica.conta(10L, Plano.BASICO);
        Usuario adulto = Fabrica.usuario(1L, TipoUsuario.ASSINANTE, Perfil.ADULTO, conta);
        Filme filmePremium = Fabrica.filme(5L, "Filme Premium", Perfil.INFANTIL, Plano.PREMIUM);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adulto));
        when(filmeRepository.findById(5L)).thenReturn(Optional.of(filmePremium));

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> filmeService.assistir(5L, 1L));

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(erro.getMessage()).contains("plano");
        verify(historicoRepository, never()).save(any());
    }

    @Test
    void assistir_permitido_deveSalvarNoHistorico() {
        Conta conta = Fabrica.conta(10L, Plano.BASICO);
        Usuario adolescente = Fabrica.usuario(3L, TipoUsuario.NAO_ASSINANTE, Perfil.ADOLESCENTE, conta);
        Filme filme = Fabrica.filme(5L, "Aventura", Perfil.ADOLESCENTE, Plano.BASICO);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(adolescente));
        when(filmeRepository.findById(5L)).thenReturn(Optional.of(filme));
        when(historicoRepository.save(any(RegistroHistorico.class))).thenAnswer(i -> i.getArgument(0));

        RegistroHistorico registro = filmeService.assistir(5L, 3L);

        assertThat(registro.getUsuario()).isSameAs(adolescente);
        assertThat(registro.getFilme()).isSameAs(filme);
        verify(historicoRepository).save(any(RegistroHistorico.class));
    }

    // ---------- Histórico ----------

    @Test
    void historico_doProprioUsuario_devolveOsRegistrosNaOrdemDoRepositorio() {
        Conta conta = Fabrica.conta(10L, Plano.BASICO);
        Usuario usuario = Fabrica.usuario(2L, TipoUsuario.NAO_ASSINANTE, Perfil.INFANTIL, conta);
        Filme filme = Fabrica.filme(5L, "Filme", Perfil.INFANTIL, Plano.BASICO);
        List<RegistroHistorico> registros = List.of(
                new RegistroHistorico(usuario, filme), new RegistroHistorico(usuario, filme));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(historicoRepository.findByUsuarioIdOrderByIdAsc(2L)).thenReturn(registros);

        List<RegistroHistorico> resultado = filmeService.historico(2L, 2L);

        assertThat(resultado).isEqualTo(registros);
    }

    @Test
    void historico_deOutroUsuarioNaoAssinante_deveSerNegado() {
        Conta conta = Fabrica.conta(10L, Plano.BASICO);
        Usuario alvo = Fabrica.usuario(2L, TipoUsuario.NAO_ASSINANTE, Perfil.INFANTIL, conta);
        Usuario outro = Fabrica.usuario(3L, TipoUsuario.NAO_ASSINANTE, Perfil.ADOLESCENTE, conta);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(alvo));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(outro));

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> filmeService.historico(2L, 3L));

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(historicoRepository, never()).findByUsuarioIdOrderByIdAsc(any());
    }
}
