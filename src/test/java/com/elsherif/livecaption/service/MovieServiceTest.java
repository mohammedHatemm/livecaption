package com.elsherif.livecaption.service;

import com.elsherif.livecaption.dto.response.GenreResponse;
import com.elsherif.livecaption.dto.response.MovieResponse;
import com.elsherif.livecaption.dto.response.PagedResponse;
import com.elsherif.livecaption.dto.tmdb.TMDBGenresResponse;
import com.elsherif.livecaption.dto.tmdb.TMDBMovie;
import com.elsherif.livecaption.dto.tmdb.TMDBMovieDetails;
import com.elsherif.livecaption.dto.tmdb.TMDBSearchResponse;
import com.elsherif.livecaption.entity.Movie;
import com.elsherif.livecaption.enums.TimeWindow;
import com.elsherif.livecaption.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private TMDBService tmdbService;

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    private TMDBMovie tmdbMovie;
    private TMDBSearchResponse tmdbSearchResponse;
    private Movie movie;

    @BeforeEach
    void setUp() {
        tmdbMovie = new TMDBMovie();
        tmdbMovie.setId(550L);
        tmdbMovie.setTitle("Fight Club");
        tmdbMovie.setOverview("A movie about fight club");
        tmdbMovie.setPosterPath("/poster.jpg");
        tmdbMovie.setBackdropPath("/backdrop.jpg");
        tmdbMovie.setReleaseDate("1999-10-15");
        tmdbMovie.setVoteAverage(8.4);
        tmdbMovie.setVoteCount(25000);

        tmdbSearchResponse = new TMDBSearchResponse();
        tmdbSearchResponse.setPage(1);
        tmdbSearchResponse.setTotalPages(10);
        tmdbSearchResponse.setTotalResults(100);
        tmdbSearchResponse.setResults(List.of(tmdbMovie));

        movie = Movie.builder()
                .id(1L)
                .tmdbId(550L)
                .title("Fight Club")
                .overview("A movie about fight club")
                .posterPath("/poster.jpg")
                .backdropPath("/backdrop.jpg")
                .releaseDate(LocalDate.of(1999, 10, 15))
                .voteAverage(BigDecimal.valueOf(8.4))
                .voteCount(25000)
                .build();
    }

    @Test
    void searchMovies_shouldReturnPagedResponse() {
        when(tmdbService.searchMovies(anyString(), anyInt())).thenReturn(tmdbSearchResponse);

        PagedResponse<MovieResponse> response = movieService.searchMovies("Fight Club", 1);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().getTitle()).isEqualTo("Fight Club");
        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(100);
        verify(tmdbService).searchMovies("Fight Club", 1);
    }

    @Test
    void getMovieDetails_shouldReturnCachedMovie() {
        when(movieRepository.findByTmdbId(anyLong())).thenReturn(Optional.of(movie));

        MovieResponse response = movieService.getMovieDetails(550L);

        assertThat(response.getTmdbId()).isEqualTo(550L);
        assertThat(response.getTitle()).isEqualTo("Fight Club");
        verify(movieRepository).findByTmdbId(550L);
        verify(tmdbService, never()).getMovieDetails(anyLong());
    }

    @Test
    void getMovieDetails_shouldFetchAndCacheWhenNotFound() {
        TMDBMovieDetails tmdbDetails = new TMDBMovieDetails();
        tmdbDetails.setId(550L);
        tmdbDetails.setTitle("Fight Club");
        tmdbDetails.setOverview("A movie about fight club");
        tmdbDetails.setReleaseDate("1999-10-15");

        when(movieRepository.findByTmdbId(anyLong())).thenReturn(Optional.empty());
        when(tmdbService.getMovieDetails(anyLong())).thenReturn(tmdbDetails);
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);

        MovieResponse response = movieService.getMovieDetails(550L);

        assertThat(response.getTitle()).isEqualTo("Fight Club");
        verify(tmdbService).getMovieDetails(550L);
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    void getPopularMovies_shouldReturnPagedResponse() {
        when(tmdbService.getPopularMovies(anyInt())).thenReturn(tmdbSearchResponse);

        PagedResponse<MovieResponse> response = movieService.getPopularMovies(1);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getPage()).isEqualTo(1);
        verify(tmdbService).getPopularMovies(1);
    }

    @Test
    void getTrendingMovies_shouldReturnPagedResponse() {
        when(tmdbService.getTrendingMovies(any(TimeWindow.class), anyInt())).thenReturn(tmdbSearchResponse);

        PagedResponse<MovieResponse> response = movieService.getTrendingMovies(TimeWindow.WEEK, 1);

        assertThat(response.getContent()).hasSize(1);
        verify(tmdbService).getTrendingMovies(TimeWindow.WEEK, 1);
    }

    @Test
    void discoverMovies_shouldReturnPagedResponse() {
        when(tmdbService.discoverMovies(any(), any(), anyInt())).thenReturn(tmdbSearchResponse);

        PagedResponse<MovieResponse> response = movieService.discoverMovies(28, 2020, 1);

        assertThat(response.getContent()).hasSize(1);
        verify(tmdbService).discoverMovies(28, 2020, 1);
    }

    @Test
    void getGenres_shouldReturnGenreList() {
        TMDBGenresResponse.Genre genre1 = new TMDBGenresResponse.Genre();
        genre1.setId(28);
        genre1.setName("Action");

        TMDBGenresResponse.Genre genre2 = new TMDBGenresResponse.Genre();
        genre2.setId(35);
        genre2.setName("Comedy");

        TMDBGenresResponse genresResponse = new TMDBGenresResponse();
        genresResponse.setGenres(List.of(genre1, genre2));

        when(tmdbService.getGenres()).thenReturn(genresResponse);

        List<GenreResponse> response = movieService.getGenres();

        assertThat(response).hasSize(2);
        assertThat(response.get(0).getName()).isEqualTo("Action");
        assertThat(response.get(1).getName()).isEqualTo("Comedy");
    }
}
