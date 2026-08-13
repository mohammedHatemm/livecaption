package com.elsherif.livecaption.controller;

import com.elsherif.livecaption.dto.response.GenreResponse;
import com.elsherif.livecaption.dto.response.MovieResponse;
import com.elsherif.livecaption.dto.response.PagedResponse;
import com.elsherif.livecaption.enums.TimeWindow;
import com.elsherif.livecaption.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@Tag(name = "Movies", description = "Movie discovery and search endpoints")
public class MovieController {

    private final MovieService movieService;

    @GetMapping("/search")
    @Operation(summary = "Search movies", description = "Search for movies by title or keywords")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned"),
            @ApiResponse(responseCode = "400", description = "Invalid search query")
    })
    public ResponseEntity<PagedResponse<MovieResponse>> searchMovies(
            @Parameter(description = "Search query") @RequestParam String query,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(movieService.searchMovies(query, page));
    }

    @GetMapping("/{tmdbId}")
    @Operation(summary = "Get movie details", description = "Get detailed information about a specific movie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movie details returned"),
            @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    public ResponseEntity<MovieResponse> getMovieDetails(
            @Parameter(description = "TMDB movie ID") @PathVariable Long tmdbId) {
        return ResponseEntity.ok(movieService.getMovieDetails(tmdbId));
    }

    @GetMapping("/popular")
    @Operation(summary = "Get popular movies", description = "Get a list of currently popular movies")
    public ResponseEntity<PagedResponse<MovieResponse>> getPopularMovies(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(movieService.getPopularMovies(page));
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending movies", description = "Get trending movies for a specific time window")
    public ResponseEntity<PagedResponse<MovieResponse>> getTrendingMovies(
            @Parameter(description = "Time window (DAY or WEEK)") @RequestParam(defaultValue = "WEEK") TimeWindow timeWindow,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(movieService.getTrendingMovies(timeWindow, page));
    }

    @GetMapping("/discover")
    @Operation(summary = "Discover movies", description = "Discover movies with optional filters")
    public ResponseEntity<PagedResponse<MovieResponse>> discoverMovies(
            @Parameter(description = "Filter by genre ID") @RequestParam(required = false) Integer genreId,
            @Parameter(description = "Filter by release year") @RequestParam(required = false) Integer year,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(movieService.discoverMovies(genreId, year, page));
    }

    @GetMapping("/genres")
    @Operation(summary = "Get all genres", description = "Get a list of all available movie genres")
    public ResponseEntity<List<GenreResponse>> getGenres() {
        return ResponseEntity.ok(movieService.getGenres());
    }
}
