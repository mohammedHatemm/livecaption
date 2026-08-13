package com.elsherif.livecaption.controller;

import com.elsherif.livecaption.dto.response.GenreResponse;
import com.elsherif.livecaption.dto.response.MovieResponse;
import com.elsherif.livecaption.dto.response.PagedResponse;
import com.elsherif.livecaption.enums.TimeWindow;
import com.elsherif.livecaption.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieControllerTest {

    @Mock
    private MovieService movieService;

    @InjectMocks
    private MovieController movieController;

    private MovieResponse movieResponse;
    private PagedResponse<MovieResponse> pagedResponse;

    @BeforeEach
    void setUp() {
        movieResponse = MovieResponse.builder()
                .tmdbId(550L)
                .title("Fight Club")
                .overview("A movie about fight club")
                .voteAverage(BigDecimal.valueOf(8.4))
                .build();

        pagedResponse = PagedResponse.<MovieResponse>builder()
                .content(List.of(movieResponse))
                .page(1)
                .totalPages(10)
                .totalElements(100L)
                .build();
    }

    @Test
    void searchMovies_shouldReturnResults() {
        when(movieService.searchMovies(anyString(), anyInt())).thenReturn(pagedResponse);

        ResponseEntity<PagedResponse<MovieResponse>> response = movieController.searchMovies("Fight Club", 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().getFirst().getTitle()).isEqualTo("Fight Club");
        assertThat(response.getBody().getPage()).isEqualTo(1);
        assertThat(response.getBody().getTotalElements()).isEqualTo(100);
    }

    @Test
    void getMovieDetails_shouldReturnMovie() {
        when(movieService.getMovieDetails(anyLong())).thenReturn(movieResponse);

        ResponseEntity<MovieResponse> response = movieController.getMovieDetails(550L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTmdbId()).isEqualTo(550L);
        assertThat(response.getBody().getTitle()).isEqualTo("Fight Club");
    }

    @Test
    void getPopularMovies_shouldReturnResults() {
        when(movieService.getPopularMovies(anyInt())).thenReturn(pagedResponse);

        ResponseEntity<PagedResponse<MovieResponse>> response = movieController.getPopularMovies(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void getTrendingMovies_shouldReturnResults() {
        when(movieService.getTrendingMovies(any(TimeWindow.class), anyInt())).thenReturn(pagedResponse);

        ResponseEntity<PagedResponse<MovieResponse>> response = movieController.getTrendingMovies(TimeWindow.WEEK, 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void discoverMovies_shouldReturnResults() {
        when(movieService.discoverMovies(any(), any(), anyInt())).thenReturn(pagedResponse);

        ResponseEntity<PagedResponse<MovieResponse>> response = movieController.discoverMovies(28, 2020, 1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getGenres_shouldReturnGenreList() {
        List<GenreResponse> genres = List.of(
                new GenreResponse(28, "Action"),
                new GenreResponse(35, "Comedy")
        );

        when(movieService.getGenres()).thenReturn(genres);

        ResponseEntity<List<GenreResponse>> response = movieController.getGenres();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Action");
    }
}
