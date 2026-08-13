package com.elsherif.livecaption.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TMDBMovie {
    private Long id;
    private String title;
    private String overview;

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

    @JsonProperty("genre_ids")
    private List<Integer> genreIds;

    @JsonProperty("original_language")
    private String originalLanguage;

    private Boolean adult;
}
