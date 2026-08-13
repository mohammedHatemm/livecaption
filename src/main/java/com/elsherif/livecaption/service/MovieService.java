package com.elsherif.livecaption.service;

import com.elsherif.livecaption.dto.response.GenreResponse;
import com.elsherif.livecaption.dto.response.MovieResponse;
import com.elsherif.livecaption.dto.response.PagedResponse;
import com.elsherif.livecaption.dto.tmdb.TMDBSearchResponse;
import com.elsherif.livecaption.enums.TimeWindow;
import com.elsherif.livecaption.mapper.MovieMapper;
import com.elsherif.livecaption.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final TMDBService tmdbService;
    private final MovieRepository movieRepository;

    public PagedResponse<MovieResponse> searchMovies(String query, int page) {
        var tmdbResponse = tmdbService.searchMovies(query, page);
        return toPagedResponse(tmdbResponse);
    }

    public MovieResponse getMovieDetails(Long tmdbId) {
        return movieRepository.findByTmdbId(tmdbId)
                .map(MovieMapper::toResponse)
                .orElseGet(() -> {
                    var tmdbMovie = tmdbService.getMovieDetails(tmdbId);
                    var movie = MovieMapper.fromTMDBDetails(tmdbMovie);
                    movieRepository.save(movie);
                    return MovieMapper.toResponse(movie);
                });
    }

    public PagedResponse<MovieResponse> getPopularMovies(int page) {
        var tmdbResponse = tmdbService.getPopularMovies(page);
        return toPagedResponse(tmdbResponse);
    }

    public PagedResponse<MovieResponse> getTrendingMovies(TimeWindow timeWindow, int page) {
        var tmdbResponse = tmdbService.getTrendingMovies(timeWindow, page);
        return toPagedResponse(tmdbResponse);
    }

    public PagedResponse<MovieResponse> discoverMovies(Integer genreId, Integer year, int page) {
        var tmdbResponse = tmdbService.discoverMovies(genreId, year, page);
        return toPagedResponse(tmdbResponse);
    }

    public List<GenreResponse> getGenres() {
        return tmdbService.getGenres().getGenres().stream()
                .map(g -> new GenreResponse(g.getId(), g.getName()))
                .toList();
    }

    private PagedResponse<MovieResponse> toPagedResponse(TMDBSearchResponse tmdb) {
        var movies = tmdb.getResults().stream()
                .map(MovieMapper::fromTMDBMovie)
                .toList();

        return PagedResponse.<MovieResponse>builder()
                .content(movies)
                .page(tmdb.getPage())
                .totalPages(tmdb.getTotalPages())
                .totalElements(tmdb.getTotalResults().longValue())
                .build();
    }
}
