package com.exemplo.streaming;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroHistoricoRepository extends JpaRepository<RegistroHistorico, Long> {

    // Ordem em que foram assistidos: o id cresce a cada novo registro
    List<RegistroHistorico> findByUsuarioIdOrderByIdAsc(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);
}
