package com.exemplo.streaming;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);

    // Quantos usuários de um tipo existem na conta (usado para a cota de compartilhamento)
    long countByContaIdAndTipo(Long contaId, TipoUsuario tipo);
}
