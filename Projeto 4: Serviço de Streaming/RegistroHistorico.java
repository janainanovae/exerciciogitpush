package com.exemplo.streaming;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Cada vez que um usuário assiste a um filme é criado um registro novo.
// Assistir ao mesmo filme duas vezes gera dois registros, cada um na sua posição da linha do tempo.
@Entity
@Table(name = "registros_historico")
public class RegistroHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "filme_id", nullable = false)
    private Filme filme;

    private LocalDateTime assistidoEm;

    protected RegistroHistorico() {
    }

    public RegistroHistorico(Usuario usuario, Filme filme) {
        this.usuario = usuario;
        this.filme = filme;
        this.assistidoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Filme getFilme() {
        return filme;
    }

    public LocalDateTime getAssistidoEm() {
        return assistidoEm;
    }
}
