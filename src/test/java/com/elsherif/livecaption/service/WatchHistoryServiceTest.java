package com.elsherif.livecaption.service;

import com.elsherif.livecaption.dto.request.UpdateProgressRequest;
import com.elsherif.livecaption.dto.response.MovieResponse;
import com.elsherif.livecaption.dto.response.PagedResponse;
import com.elsherif.livecaption.dto.response.WatchHistoryResponse;
import com.elsherif.livecaption.entity.User;
import com.elsherif.livecaption.entity.WatchHistory;
import com.elsherif.livecaption.enums.Role;
import com.elsherif.livecaption.repository.UserRepository;
import com.elsherif.livecaption.repository.WatchHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchHistoryServiceTest {

    @Mock
    private WatchHistoryRepository watchHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MovieService movieService;

    @InjectMocks
    private WatchHistoryService watchHistoryService;

    private User testUser;
    private WatchHistory watchHistory;
    private MovieResponse movieResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .passwordHash("encodedPassword")
                .role(Role.USER)
                .build();

        watchHistory = WatchHistory.builder()
                .id(1L)
                .user(testUser)
                .tmdbId(550L)
                .progressSeconds(3600)
                .durationSeconds(8100)
                .completed(false)
                .lastWatchedAt(LocalDateTime.now())
                .build();

        movieResponse = MovieResponse.builder()
                .tmdbId(550L)
                .title("Fight Club")
                .overview("A movie about fight club")
                .voteAverage(BigDecimal.valueOf(8.4))
                .build();
    }

    @Test
    void getWatchHistory_shouldReturnPagedResponse() {
        var historyPage = new PageImpl<>(List.of(watchHistory), PageRequest.of(0, 20), 1);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(watchHistoryRepository.findByUserIdOrderByLastWatchedAtDesc(anyLong(), any(PageRequest.class)))
                .thenReturn(historyPage);
        when(movieService.getMovieDetails(anyLong())).thenReturn(movieResponse);

        PagedResponse<WatchHistoryResponse> response = watchHistoryService.getWatchHistory("test@example.com", 1, 20);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().getTmdbId()).isEqualTo(550L);
        assertThat(response.getContent().getFirst().getProgressSeconds()).isEqualTo(3600);
        assertThat(response.getContent().getFirst().getProgressPercentage()).isEqualTo(44);
        assertThat(response.getPage()).isEqualTo(1);
    }

    @Test
    void getContinueWatching_shouldReturnUnfinishedMovies() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(watchHistoryRepository.findByUserIdAndCompletedFalseOrderByLastWatchedAtDesc(anyLong(), any(PageRequest.class)))
                .thenReturn(List.of(watchHistory));
        when(movieService.getMovieDetails(anyLong())).thenReturn(movieResponse);

        List<WatchHistoryResponse> response = watchHistoryService.getContinueWatching("test@example.com", 10);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getCompleted()).isFalse();
        assertThat(response.getFirst().getMovie().getTitle()).isEqualTo("Fight Club");
    }

    @Test
    void updateProgress_shouldUpdateExistingHistory() {
        UpdateProgressRequest request = new UpdateProgressRequest(5400, 8100);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(watchHistoryRepository.findByUserIdAndTmdbId(anyLong(), anyLong())).thenReturn(Optional.of(watchHistory));
        when(watchHistoryRepository.save(any(WatchHistory.class))).thenReturn(watchHistory);
        when(movieService.getMovieDetails(anyLong())).thenReturn(movieResponse);

        WatchHistoryResponse response = watchHistoryService.updateProgress("test@example.com", 550L, request);

        assertThat(response.getTmdbId()).isEqualTo(550L);
        verify(watchHistoryRepository).save(any(WatchHistory.class));
    }

    @Test
    void updateProgress_shouldCreateNewHistoryWhenNotExists() {
        UpdateProgressRequest request = new UpdateProgressRequest(1800, 8100);

        WatchHistory newHistory = WatchHistory.builder()
                .id(2L)
                .user(testUser)
                .tmdbId(999L)
                .progressSeconds(1800)
                .durationSeconds(8100)
                .completed(false)
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(watchHistoryRepository.findByUserIdAndTmdbId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(watchHistoryRepository.save(any(WatchHistory.class))).thenReturn(newHistory);
        when(movieService.getMovieDetails(anyLong())).thenReturn(movieResponse);

        WatchHistoryResponse response = watchHistoryService.updateProgress("test@example.com", 999L, request);

        assertThat(response).isNotNull();
        verify(watchHistoryRepository).save(any(WatchHistory.class));
    }

    @Test
    void updateProgress_shouldMarkAsCompletedWhenOver90Percent() {
        UpdateProgressRequest request = new UpdateProgressRequest(7500, 8100);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(watchHistoryRepository.findByUserIdAndTmdbId(anyLong(), anyLong())).thenReturn(Optional.of(watchHistory));
        when(watchHistoryRepository.save(any(WatchHistory.class))).thenAnswer(invocation -> {
            WatchHistory saved = invocation.getArgument(0);
            assertThat(saved.getCompleted()).isTrue();
            return saved;
        });
        when(movieService.getMovieDetails(anyLong())).thenReturn(movieResponse);

        watchHistoryService.updateProgress("test@example.com", 550L, request);

        verify(watchHistoryRepository).save(argThat(h -> h.getCompleted()));
    }

    @Test
    void deleteHistory_shouldRemoveSuccessfully() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        doNothing().when(watchHistoryRepository).deleteByUserIdAndTmdbId(anyLong(), anyLong());

        watchHistoryService.deleteHistory("test@example.com", 550L);

        verify(watchHistoryRepository).deleteByUserIdAndTmdbId(1L, 550L);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> watchHistoryService.getWatchHistory("notfound@example.com", 1, 20))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }
}
