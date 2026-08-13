package com.elsherif.livecaption.ai.recommender;

import com.elsherif.livecaption.ai.common.EmbeddingService;
import com.elsherif.livecaption.dto.response.MovieResponse;
import com.elsherif.livecaption.entity.Movie;
import com.elsherif.livecaption.entity.WatchHistory;
import com.elsherif.livecaption.repository.MovieRepository;
import com.elsherif.livecaption.repository.WatchHistoryRepository;
import com.elsherif.livecaption.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI-powered recommendation engine using embeddings.
 * Generates recommendations based on:
 * 1. Movie content similarity (plot, genre)
 * 2. User watch history preferences
 * 3. Collaborative filtering patterns
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final EmbeddingService embeddingService;
    private final MovieRepository movieRepository;
    private final WatchHistoryRepository watchHistoryRepository;
    private final MovieService movieService;

    // Simple in-memory cache for movie embeddings
    private final Map<Long, float[]> movieEmbeddingCache = new HashMap<>();

    /**
     * Get personalized recommendations for a user based on their watch history
     */
    public List<MovieResponse> getPersonalizedRecommendations(Long userId, int limit) {
        // Get user's watch history
        List<WatchHistory> history = watchHistoryRepository
                .findByUserIdOrderByLastWatchedAtDesc(userId, PageRequest.of(0, 20))
                .getContent();

        if (history.isEmpty()) {
            // No history, return popular movies
            return movieService.getPopularMovies(1).getContent().stream()
                    .limit(limit)
                    .toList();
        }

        // Get watched movie IDs
        Set<Long> watchedTmdbIds = history.stream()
                .map(WatchHistory::getTmdbId)
                .collect(Collectors.toSet());

        // Calculate user preference embedding
        float[] userPreferenceEmbedding = calculateUserPreferenceEmbedding(history);

        // Find similar movies not yet watched
        return findSimilarMovies(userPreferenceEmbedding, watchedTmdbIds, limit);
    }

    /**
     * Get similar movies based on a specific movie
     */
    public List<MovieResponse> getSimilarMovies(Long tmdbId, int limit) {
        // Get the source movie embedding
        float[] sourceEmbedding = getMovieEmbedding(tmdbId);
        if (sourceEmbedding == null) {
            return Collections.emptyList();
        }

        return findSimilarMovies(sourceEmbedding, Set.of(tmdbId), limit);
    }

    /**
     * Calculate user preference embedding from watch history
     */
    private float[] calculateUserPreferenceEmbedding(List<WatchHistory> history) {
        List<float[]> embeddings = new ArrayList<>();
        
        for (WatchHistory item : history) {
            float[] embedding = getMovieEmbedding(item.getTmdbId());
            if (embedding != null) {
                // Weight by completion - fully watched movies have more influence
                float weight = item.getCompleted() ? 1.0f : 0.5f;
                float[] weightedEmbedding = new float[embedding.length];
                for (int i = 0; i < embedding.length; i++) {
                    weightedEmbedding[i] = embedding[i] * weight;
                }
                embeddings.add(weightedEmbedding);
            }
        }

        if (embeddings.isEmpty()) {
            return null;
        }

        // Average all embeddings
        int dimension = embeddings.getFirst().length;
        float[] avgEmbedding = new float[dimension];
        for (float[] embedding : embeddings) {
            for (int i = 0; i < dimension; i++) {
                avgEmbedding[i] += embedding[i];
            }
        }
        for (int i = 0; i < dimension; i++) {
            avgEmbedding[i] /= embeddings.size();
        }

        return avgEmbedding;
    }

    /**
     * Get or generate embedding for a movie
     */
    private float[] getMovieEmbedding(Long tmdbId) {
        // Check cache first
        if (movieEmbeddingCache.containsKey(tmdbId)) {
            return movieEmbeddingCache.get(tmdbId);
        }

        // Get movie from database or TMDB
        MovieResponse movie = movieService.getMovieDetails(tmdbId);
        if (movie == null) {
            return null;
        }

        // Generate embedding from movie description
        String movieText = buildMovieText(movie);
        float[] embedding = embeddingService.embed(movieText);

        // Cache it
        movieEmbeddingCache.put(tmdbId, embedding);

        return embedding;
    }

    /**
     * Build text representation of a movie for embedding
     */
    private String buildMovieText(MovieResponse movie) {
        StringBuilder sb = new StringBuilder();
        sb.append(movie.getTitle()).append(". ");
        
        if (movie.getOverview() != null) {
            sb.append(movie.getOverview()).append(" ");
        }
        
        if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
            sb.append("Genres: ");
            sb.append(movie.getGenres().stream()
                    .map(MovieResponse.GenreResponse::getName)
                    .collect(Collectors.joining(", ")));
        }
        
        return sb.toString();
    }

    /**
     * Find movies similar to the given embedding
     */
    private List<MovieResponse> findSimilarMovies(float[] targetEmbedding, Set<Long> excludeTmdbIds, int limit) {
        if (targetEmbedding == null) {
            return Collections.emptyList();
        }

        // Get candidate movies from database
        List<Movie> candidates = movieRepository.findAll();

        // Calculate similarity scores
        List<ScoredMovie> scoredMovies = new ArrayList<>();
        for (Movie movie : candidates) {
            if (excludeTmdbIds.contains(movie.getTmdbId())) {
                continue;
            }

            float[] movieEmbedding = getMovieEmbedding(movie.getTmdbId());
            if (movieEmbedding != null) {
                double similarity = cosineSimilarity(targetEmbedding, movieEmbedding);
                scoredMovies.add(new ScoredMovie(movie.getTmdbId(), similarity));
            }
        }

        // Sort by similarity and get top results
        return scoredMovies.stream()
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(limit)
                .map(sm -> movieService.getMovieDetails(sm.tmdbId))
                .toList();
    }

    /**
     * Calculate cosine similarity between two vectors
     */
    private double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return denominator > 0 ? dotProduct / denominator : 0.0;
    }

    private record ScoredMovie(Long tmdbId, double score) {}
}
