package com.elsherif.livecaption.service;

import com.elsherif.livecaption.dto.tmdb.TMDBGenresResponse;
import com.elsherif.livecaption.dto.tmdb.TMDBMovieDetails;
import com.elsherif.livecaption.dto.tmdb.TMDBSearchResponse;
import com.elsherif.livecaption.enums.TimeWindow;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class TMDBService {

    private final WebClient tmdbWebClient;

    @Value("${tmdb.image-base-url}")
    private String imageBaseUrl;

    public TMDBSearchResponse searchMovies(String query, int page) {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/movie")
                        .queryParam("query", query)
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(TMDBSearchResponse.class)
                .block();
    }

    public TMDBMovieDetails getMovieDetails(Long tmdbId) {
        return tmdbWebClient.get()
                .uri("/movie/{id}", tmdbId)
                .retrieve()
                .bodyToMono(TMDBMovieDetails.class)
                .block();
    }

    public TMDBSearchResponse getPopularMovies(int page) {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/popular")
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(TMDBSearchResponse.class)
                .block();
    }

    public TMDBSearchResponse getTrendingMovies(TimeWindow timeWindow, int page) {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/trending/movie/{timeWindow}")
                        .queryParam("page", page)
                        .build(timeWindow.getValue()))
                .retrieve()
                .bodyToMono(TMDBSearchResponse.class)
                .block();
    }

    public TMDBGenresResponse getGenres() {
        return tmdbWebClient.get()
                .uri("/genre/movie/list")
                .retrieve()
                .bodyToMono(TMDBGenresResponse.class)
                .block();
    }

    public TMDBSearchResponse discoverMovies(Integer genreId, Integer year, int page) {
        return tmdbWebClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/discover/movie").queryParam("page", page);
                    if (genreId != null) builder.queryParam("with_genres", genreId);
                    if (year != null) builder.queryParam("year", year);
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(TMDBSearchResponse.class)
                .block();
    }

    public String buildImageUrl(String path, String size) {
        if (path == null) return null;
        return imageBaseUrl + "/" + size + path;
    }
}
