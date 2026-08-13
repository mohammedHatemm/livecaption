package com.elsherif.livecaption.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TMDBMovieDetails {
    private Long id;
    private String title;
    private String overview;
    private String tagline;
    private Integer runtime;
    private String status;
    private Long budget;
    private Long revenue;
    private String homepage;

    @JsonProperty("imdb_id")
    private String imdbId;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("vote_count")
    private Integer voteCount;

    private Double popularity;

    @JsonProperty("original_language")
    private String originalLanguage;

    @JsonProperty("original_title")
    private String originalTitle;

    private Boolean adult;
    private List<Genre> genres;

    @JsonProperty("production_companies")
    private List<ProductionCompany> productionCompanies;

    @Data
    public static class Genre {
        private Integer id;
        private String name;
    }

    @Data
    public static class ProductionCompany {
        private Integer id;
        private String name;

        @JsonProperty("logo_path")
        private String logoPath;

        @JsonProperty("origin_country")
        private String originCountry;
    }
}
