package com.elsherif.livecaption.mapper;

import com.elsherif.livecaption.dto.response.MovieResponse;
import com.elsherif.livecaption.dto.tmdb.TMDBMovie;
import com.elsherif.livecaption.dto.tmdb.TMDBMovieDetails;
import com.elsherif.livecaption.entity.Movie;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class MovieMapper {

    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p";

    public static MovieResponse toResponse(Movie movie) {
        if (movie == null) return null;

        List<MovieResponse.GenreResponse> genres = movie.getGenres() == null ? Collections.emptyList() :
                movie.getGenres().stream()
                        .map(g -> new MovieResponse.GenreResponse(g.getId(), g.getName()))
                        .toList();

        return MovieResponse.builder()
                .id(movie.getId())
                .tmdbId(movie.getTmdbId())
                .title(movie.getTitle())
                .overview(movie.getOverview())
                .posterUrl(buildImageUrl(movie.getPosterPath(), "w500"))
                .backdropUrl(buildImageUrl(movie.getBackdropPath(), "w1280"))
                .releaseDate(movie.getReleaseDate())
                .genres(genres)
                .voteAverage(movie.getVoteAverage())
                .voteCount(movie.getVoteCount())
                .runtime(movie.getRuntime())
                .build();
    }

    public static Movie fromTMDBDetails(TMDBMovieDetails tmdb) {
        if (tmdb == null) return null;

        List<Movie.Genre> genres = tmdb.getGenres() == null ? Collections.emptyList() :
                tmdb.getGenres().stream()
                        .map(g -> new Movie.Genre(g.getId(), g.getName()))
                        .toList();

        return Movie.builder()
                .tmdbId(tmdb.getId())
                .title(tmdb.getTitle())
                .overview(tmdb.getOverview())
                .posterPath(tmdb.getPosterPath())
                .backdropPath(tmdb.getBackdropPath())
                .releaseDate(parseDate(tmdb.getReleaseDate()))
                .genres(genres)
                .voteAverage(tmdb.getVoteAverage() != null ? BigDecimal.valueOf(tmdb.getVoteAverage()) : null)
                .voteCount(tmdb.getVoteCount())
                .runtime(tmdb.getRuntime())
                .build();
    }

    public static MovieResponse fromTMDBMovie(TMDBMovie tmdb) {
        if (tmdb == null) return null;

        return MovieResponse.builder()
                .tmdbId(tmdb.getId())
                .title(tmdb.getTitle())
                .overview(tmdb.getOverview())
                .posterUrl(buildImageUrl(tmdb.getPosterPath(), "w500"))
                .backdropUrl(buildImageUrl(tmdb.getBackdropPath(), "w1280"))
                .releaseDate(parseDate(tmdb.getReleaseDate()))
                .voteAverage(tmdb.getVoteAverage() != null ? BigDecimal.valueOf(tmdb.getVoteAverage()) : null)
                .voteCount(tmdb.getVoteCount())
                .build();
    }

    private static String buildImageUrl(String path, String size) {
        return path != null ? IMAGE_BASE_URL + "/" + size + path : null;
    }

    private static LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) return null;
        try {
            return LocalDate.parse(date);
        } catch (Exception e) {
            return null;
        }
    }
}
