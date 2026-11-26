package com.exemplo.streaming;

public record UsuarioResponse(Long id, String nome, String email, TipoUsuario tipo, Perfil perfil,
                              Long contaId, Plano plano) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTipo(),
                usuario.getPerfil(), usuario.getConta().getId(), usuario.getConta().getPlano());
    }
}
