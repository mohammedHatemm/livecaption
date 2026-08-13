package com.elsherif.livecaption.mapper;

import com.elsherif.livecaption.dto.tmdb.TMDBMovie;
import com.elsherif.livecaption.dto.tmdb.TMDBMovieDetails;
import com.elsherif.livecaption.entity.Movie;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MovieMapperTest {

    @Test
    void shouldMapEntityToResponse() {
        var movie = Movie.builder()
                .id(1L)
                .tmdbId(123L)
                .title("Test Movie")
                .overview("Test overview")
                .posterPath("/poster.jpg")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .voteAverage(BigDecimal.valueOf(8.5))
                .build();

        var response = MovieMapper.toResponse(movie);

        assertThat(response.getTmdbId()).isEqualTo(123L);
        assertThat(response.getTitle()).isEqualTo("Test Movie");
        assertThat(response.getPosterUrl()).contains("/poster.jpg");
    }

    @Test
    void shouldMapTMDBDetailsToEntity() {
        var tmdb = new TMDBMovieDetails();
        tmdb.setId(123L);
        tmdb.setTitle("Test Movie");
        tmdb.setOverview("Overview");
        tmdb.setReleaseDate("2024-01-01");
        tmdb.setVoteAverage(8.5);

        var movie = MovieMapper.fromTMDBDetails(tmdb);

        assertThat(movie.getTmdbId()).isEqualTo(123L);
        assertThat(movie.getTitle()).isEqualTo("Test Movie");
        assertThat(movie.getReleaseDate()).isEqualTo(LocalDate.of(2024, 1, 1));
    }

    @Test
    void shouldHandleNullPosterPath() {
        var movie = Movie.builder()
                .id(1L)
                .tmdbId(123L)
                .title("Test")
                .posterPath(null)
                .build();

        var response = MovieMapper.toResponse(movie);

        assertThat(response.getPosterUrl()).isNull();
    }

    @Test
    void shouldHandleInvalidDate() {
        var tmdb = new TMDBMovieDetails();
        tmdb.setId(123L);
        tmdb.setTitle("Test");
        tmdb.setReleaseDate("invalid-date");

        var movie = MovieMapper.fromTMDBDetails(tmdb);

        assertThat(movie.getReleaseDate()).isNull();
    }
}
