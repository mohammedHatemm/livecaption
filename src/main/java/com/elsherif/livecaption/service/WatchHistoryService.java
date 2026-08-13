package com.elsherif.livecaption.service;

import com.elsherif.livecaption.dto.request.UpdateProgressRequest;
import com.elsherif.livecaption.dto.response.PagedResponse;
import com.elsherif.livecaption.dto.response.WatchHistoryResponse;
import com.elsherif.livecaption.entity.User;
import com.elsherif.livecaption.entity.WatchHistory;
import com.elsherif.livecaption.repository.UserRepository;
import com.elsherif.livecaption.repository.WatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchHistoryService {

    private final WatchHistoryRepository watchHistoryRepository;
    private final UserRepository userRepository;
    private final MovieService movieService;

    public PagedResponse<WatchHistoryResponse> getWatchHistory(String email, int page, int size) {
        var user = getUser(email);
        var historyPage = watchHistoryRepository.findByUserIdOrderByLastWatchedAtDesc(user.getId(), PageRequest.of(page - 1, size));

        var items = historyPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PagedResponse.<WatchHistoryResponse>builder()
                .content(items)
                .page(page)
                .totalPages(historyPage.getTotalPages())
                .totalElements(historyPage.getTotalElements())
                .build();
    }

    public List<WatchHistoryResponse> getContinueWatching(String email, int limit) {
        var user = getUser(email);
        return watchHistoryRepository.findByUserIdAndCompletedFalseOrderByLastWatchedAtDesc(user.getId(), PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public WatchHistoryResponse updateProgress(String email, Long tmdbId, UpdateProgressRequest request) {
        var user = getUser(email);

        var history = watchHistoryRepository.findByUserIdAndTmdbId(user.getId(), tmdbId)
                .orElseGet(() -> WatchHistory.builder()
                        .user(user)
                        .tmdbId(tmdbId)
                        .build());

        history.setProgressSeconds(request.getProgressSeconds());
        if (request.getDurationSeconds() != null) {
            history.setDurationSeconds(request.getDurationSeconds());
            history.setCompleted(request.getProgressSeconds() >= request.getDurationSeconds() * 0.9);
        }

        watchHistoryRepository.save(history);
        return toResponse(history);
    }

    @Transactional
    public void deleteHistory(String email, Long tmdbId) {
        var user = getUser(email);
        watchHistoryRepository.deleteByUserIdAndTmdbId(user.getId(), tmdbId);
    }

    private WatchHistoryResponse toResponse(WatchHistory history) {
        Integer percentage = null;
        if (history.getDurationSeconds() != null && history.getDurationSeconds() > 0) {
            percentage = (int) ((history.getProgressSeconds() * 100.0) / history.getDurationSeconds());
        }

        return WatchHistoryResponse.builder()
                .id(history.getId())
                .tmdbId(history.getTmdbId())
                .movie(movieService.getMovieDetails(history.getTmdbId()))
                .progressSeconds(history.getProgressSeconds())
                .durationSeconds(history.getDurationSeconds())
                .progressPercentage(percentage)
                .completed(history.getCompleted())
                .lastWatchedAt(history.getLastWatchedAt())
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
