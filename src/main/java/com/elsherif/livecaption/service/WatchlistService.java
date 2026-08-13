package com.elsherif.livecaption.service;

import com.elsherif.livecaption.dto.response.MovieResponse;
import com.elsherif.livecaption.dto.response.PagedResponse;
import com.elsherif.livecaption.dto.response.WatchlistResponse;
import com.elsherif.livecaption.entity.User;
import com.elsherif.livecaption.entity.Watchlist;
import com.elsherif.livecaption.repository.UserRepository;
import com.elsherif.livecaption.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final MovieService movieService;

    public PagedResponse<WatchlistResponse> getUserWatchlist(String email, int page, int size) {
        var user = getUser(email);
        var watchlistPage = watchlistRepository.findByUserIdOrderByAddedAtDesc(user.getId(), PageRequest.of(page - 1, size));

        var items = watchlistPage.getContent().stream()
                .map(w -> WatchlistResponse.builder()
                        .id(w.getId())
                        .tmdbId(w.getTmdbId())
                        .movie(movieService.getMovieDetails(w.getTmdbId()))
                        .addedAt(w.getAddedAt())
                        .build())
                .toList();

        return PagedResponse.<WatchlistResponse>builder()
                .content(items)
                .page(page)
                .totalPages(watchlistPage.getTotalPages())
                .totalElements(watchlistPage.getTotalElements())
                .build();
    }

    @Transactional
    public WatchlistResponse addToWatchlist(String email, Long tmdbId) {
        var user = getUser(email);

        if (watchlistRepository.existsByUserIdAndTmdbId(user.getId(), tmdbId)) {
            throw new RuntimeException("Movie already in watchlist");
        }

        var watchlist = Watchlist.builder()
                .user(user)
                .tmdbId(tmdbId)
                .build();

        watchlistRepository.save(watchlist);
        MovieResponse movie = movieService.getMovieDetails(tmdbId);

        return WatchlistResponse.builder()
                .id(watchlist.getId())
                .tmdbId(tmdbId)
                .movie(movie)
                .addedAt(watchlist.getAddedAt())
                .build();
    }

    @Transactional
    public void removeFromWatchlist(String email, Long tmdbId) {
        var user = getUser(email);
        watchlistRepository.deleteByUserIdAndTmdbId(user.getId(), tmdbId);
    }

    public boolean isInWatchlist(String email, Long tmdbId) {
        var user = getUser(email);
        return watchlistRepository.existsByUserIdAndTmdbId(user.getId(), tmdbId);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
