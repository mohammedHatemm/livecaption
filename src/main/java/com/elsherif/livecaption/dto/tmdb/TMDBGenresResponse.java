package com.elsherif.livecaption.dto.tmdb;

import lombok.Data;

import java.util.List;

@Data
public class TMDBGenresResponse {
    private List<Genre> genres;

    @Data
    public static class Genre {
        private Integer id;
        private String name;
    }
}
