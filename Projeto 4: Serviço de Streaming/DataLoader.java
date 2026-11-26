package com.exemplo.streaming;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Cadastra um catálogo de filmes de exemplo ao iniciar a aplicação
@Component
public class DataLoader implements CommandLineRunner {

    private final FilmeRepository filmeRepository;

    public DataLoader(FilmeRepository filmeRepository) {
        this.filmeRepository = filmeRepository;
    }

    @Override
    public void run(String... args) {
        if (filmeRepository.count() > 0) {
            return;
        }

        filmeRepository.save(new Filme("Pequeno Panda", Perfil.INFANTIL, Plano.BASICO));
        filmeRepository.save(new Filme("Aventura no Espaço", Perfil.ADOLESCENTE, Plano.BASICO));
        filmeRepository.save(new Filme("Drama Urbano", Perfil.ADULTO, Plano.BASICO));
        filmeRepository.save(new Filme("Thriller Noturno", Perfil.ADULTO, Plano.PADRAO));
        filmeRepository.save(new Filme("Super Heróis em 4K", Perfil.INFANTIL, Plano.PREMIUM));
        filmeRepository.save(new Filme("Ficção Premium", Perfil.ADOLESCENTE, Plano.PREMIUM));
        System.out.println("Catálogo de exemplo cadastrado: 6 filmes.");
    }
}
