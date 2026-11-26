package com.exemplo.streaming;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ContaRepository contaRepository;

    @InjectMocks
    private ContaService contaService;

    @Test
    void migrarPlano_deveTrocarOPlanoDaConta() {
        Conta conta = Fabrica.conta(10L, Plano.BASICO);
        Usuario assinante = Fabrica.usuario(1L, TipoUsuario.ASSINANTE, Perfil.ADULTO, conta);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(assinante));
        when(usuarioRepository.countByContaIdAndTipo(10L, TipoUsuario.NAO_ASSINANTE)).thenReturn(1L);
        when(contaRepository.save(conta)).thenReturn(conta);

        Conta resultado = contaService.migrarPlano(10L, Plano.PREMIUM, 1L);

        assertThat(resultado.getPlano()).isEqualTo(Plano.PREMIUM);
        verify(contaRepository).save(conta);
    }

    @Test
    void migrarPlano_paraPlanoComCotaMenorQueOsUsuariosDaConta_deveSerRecusado() {
        Conta conta = Fabrica.conta(10L, Plano.PREMIUM);
        Usuario assinante = Fabrica.usuario(1L, TipoUsuario.ASSINANTE, Perfil.ADULTO, conta);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(assinante));
        when(usuarioRepository.countByContaIdAndTipo(10L, TipoUsuario.NAO_ASSINANTE)).thenReturn(3L);

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> contaService.migrarPlano(10L, Plano.BASICO, 1L)); // BASICO permite só 1

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(conta.getPlano()).isEqualTo(Plano.PREMIUM);
        verify(contaRepository, never()).save(any());
    }

    @Test
    void migrarPlano_porQuemNaoEAssinante_deveSerNegado() {
        Conta conta = Fabrica.conta(10L, Plano.BASICO);
        Usuario filho = Fabrica.usuario(2L, TipoUsuario.NAO_ASSINANTE, Perfil.INFANTIL, conta);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(filho));

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> contaService.migrarPlano(10L, Plano.PREMIUM, 2L));

        assertThat(erro.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(contaRepository, never()).save(any());
    }

    @Test
    void consultarCota_deveInformarTotalUsadaEDisponivel() {
        Conta conta = Fabrica.conta(10L, Plano.PADRAO); // cota de 2
        Usuario assinante = Fabrica.usuario(1L, TipoUsuario.ASSINANTE, Perfil.ADULTO, conta);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(assinante));
        when(usuarioRepository.countByContaIdAndTipo(10L, TipoUsuario.NAO_ASSINANTE)).thenReturn(1L);

        CotaResponse cota = contaService.consultarCota(10L, 1L);

        assertThat(cota.plano()).isEqualTo(Plano.PADRAO);
        assertThat(cota.cotaTotal()).isEqualTo(2);
        assertThat(cota.cotaUsada()).isEqualTo(1);
        assertThat(cota.cotaDisponivel()).isEqualTo(1);
    }
}
