package com.elsherif.livecaption.controller;

import com.elsherif.livecaption.dto.response.GenreResponse;
import com.elsherif.livecaption.dto.response.MovieResponse;
import com.elsherif.livecaption.dto.response.PagedResponse;
import com.elsherif.livecaption.enums.TimeWindow;
import com.elsherif.livecaption.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<MovieResponse>> searchMovies(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(movieService.searchMovies(query, page));
    }

    @GetMapping("/{tmdbId}")
    public ResponseEntity<MovieResponse> getMovieDetails(@PathVariable Long tmdbId) {
        return ResponseEntity.ok(movieService.getMovieDetails(tmdbId));
    }

    @GetMapping("/popular")
    public ResponseEntity<PagedResponse<MovieResponse>> getPopularMovies(
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(movieService.getPopularMovies(page));
    }

    @GetMapping("/trending")
    public ResponseEntity<PagedResponse<MovieResponse>> getTrendingMovies(
            @RequestParam(defaultValue = "WEEK") TimeWindow timeWindow,
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(movieService.getTrendingMovies(timeWindow, page));
    }

    @GetMapping("/discover")
    public ResponseEntity<PagedResponse<MovieResponse>> discoverMovies(
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "1") int page) {
        return ResponseEntity.ok(movieService.discoverMovies(genreId, year, page));
    }

    @GetMapping("/genres")
    public ResponseEntity<List<GenreResponse>> getGenres() {
        return ResponseEntity.ok(movieService.getGenres());
    }
}
