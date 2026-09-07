package br.com.alura.screnmatch.dto;

import br.com.alura.screnmatch.model.Categoria;


public record SerieDTO(Long id,
                       String titulo,
                       Integer totalTemporada,
                       Double avaliacao,
                       Categoria genero,
                       String atores,
                       String poster,
                       String sinopse) {
}
