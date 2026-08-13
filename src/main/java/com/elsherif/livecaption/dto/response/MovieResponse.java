package com.elsherif.livecaption.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {
    private Long id;
    private Long tmdbId;
    private String title;
    private String overview;
    private String posterUrl;
    private String backdropUrl;
    private LocalDate releaseDate;
    private List<GenreResponse> genres;
    private BigDecimal voteAverage;
    private Integer voteCount;
    private Integer runtime;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenreResponse {
        private Integer id;
        private String name;
    }
}
