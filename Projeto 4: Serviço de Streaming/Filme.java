package com.exemplo.streaming;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "filmes")
public class Filme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    // Perfil mínimo para assistir: INFANTIL = todos, ADOLESCENTE = adolescente e adulto, ADULTO = só adulto
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Perfil perfilMinimo;

    // Plano mínimo que inclui o filme
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Plano planoMinimo;

    protected Filme() {
    }

    public Filme(String titulo, Perfil perfilMinimo, Plano planoMinimo) {
        this.titulo = titulo;
        this.perfilMinimo = perfilMinimo;
        this.planoMinimo = planoMinimo;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public Perfil getPerfilMinimo() {
        return perfilMinimo;
    }

    public Plano getPlanoMinimo() {
        return planoMinimo;
    }
}
