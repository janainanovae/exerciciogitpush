package com.exemplo.streaming;

import org.springframework.test.util.ReflectionTestUtils;

// Cria objetos de domínio para os testes unitários. Como não há banco para gerar os ids,
// eles são definidos por reflexão.
final class Fabrica {

    private Fabrica() {
    }

    static Conta conta(Long id, Plano plano) {
        Conta conta = new Conta(plano);
        ReflectionTestUtils.setField(conta, "id", id);
        return conta;
    }

    static Usuario usuario(Long id, TipoUsuario tipo, Perfil perfil, Conta conta) {
        Usuario usuario = new Usuario("Usuario " + id, "usuario" + id + "@email.com", tipo, perfil, conta);
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    static Filme filme(Long id, String titulo, Perfil perfilMinimo, Plano planoMinimo) {
        Filme filme = new Filme(titulo, perfilMinimo, planoMinimo);
        ReflectionTestUtils.setField(filme, "id", id);
        return filme;
    }
}
