package com.elsherif.livecaption.service;

import com.elsherif.livecaption.dto.response.MovieResponse;
import com.elsherif.livecaption.dto.response.PagedResponse;
import com.elsherif.livecaption.dto.response.WatchlistResponse;
import com.elsherif.livecaption.entity.User;
import com.elsherif.livecaption.entity.Watchlist;
import com.elsherif.livecaption.enums.Role;
import com.elsherif.livecaption.repository.UserRepository;
import com.elsherif.livecaption.repository.WatchlistRepository;
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
class WatchlistServiceTest {

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MovieService movieService;

    @InjectMocks
    private WatchlistService watchlistService;

    private User testUser;
    private Watchlist watchlistItem;
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

        watchlistItem = Watchlist.builder()
                .id(1L)
                .user(testUser)
                .tmdbId(550L)
                .addedAt(LocalDateTime.now())
                .build();

        movieResponse = MovieResponse.builder()
                .tmdbId(550L)
                .title("Fight Club")
                .overview("A movie about fight club")
                .voteAverage(BigDecimal.valueOf(8.4))
                .build();
    }

    @Test
    void getUserWatchlist_shouldReturnPagedResponse() {
        var watchlistPage = new PageImpl<>(List.of(watchlistItem), PageRequest.of(0, 20), 1);
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(watchlistRepository.findByUserIdOrderByAddedAtDesc(anyLong(), any(PageRequest.class)))
                .thenReturn(watchlistPage);
        when(movieService.getMovieDetails(anyLong())).thenReturn(movieResponse);

        PagedResponse<WatchlistResponse> response = watchlistService.getUserWatchlist("test@example.com", 1, 20);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().getTmdbId()).isEqualTo(550L);
        assertThat(response.getContent().getFirst().getMovie().getTitle()).isEqualTo("Fight Club");
        assertThat(response.getPage()).isEqualTo(1);
    }

    @Test
    void addToWatchlist_shouldAddMovieSuccessfully() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(watchlistRepository.existsByUserIdAndTmdbId(anyLong(), anyLong())).thenReturn(false);
        when(watchlistRepository.save(any(Watchlist.class))).thenReturn(watchlistItem);
        when(movieService.getMovieDetails(anyLong())).thenReturn(movieResponse);

        WatchlistResponse response = watchlistService.addToWatchlist("test@example.com", 550L);

        assertThat(response.getTmdbId()).isEqualTo(550L);
        assertThat(response.getMovie().getTitle()).isEqualTo("Fight Club");
        verify(watchlistRepository).save(any(Watchlist.class));
    }

    @Test
    void addToWatchlist_shouldThrowWhenAlreadyExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(watchlistRepository.existsByUserIdAndTmdbId(anyLong(), anyLong())).thenReturn(true);

        assertThatThrownBy(() -> watchlistService.addToWatchlist("test@example.com", 550L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Movie already in watchlist");

        verify(watchlistRepository, never()).save(any(Watchlist.class));
    }

    @Test
    void removeFromWatchlist_shouldRemoveSuccessfully() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        doNothing().when(watchlistRepository).deleteByUserIdAndTmdbId(anyLong(), anyLong());

        watchlistService.removeFromWatchlist("test@example.com", 550L);

        verify(watchlistRepository).deleteByUserIdAndTmdbId(1L, 550L);
    }

    @Test
    void isInWatchlist_shouldReturnTrueWhenExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(watchlistRepository.existsByUserIdAndTmdbId(anyLong(), anyLong())).thenReturn(true);

        boolean result = watchlistService.isInWatchlist("test@example.com", 550L);

        assertThat(result).isTrue();
    }

    @Test
    void isInWatchlist_shouldReturnFalseWhenNotExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(watchlistRepository.existsByUserIdAndTmdbId(anyLong(), anyLong())).thenReturn(false);

        boolean result = watchlistService.isInWatchlist("test@example.com", 550L);

        assertThat(result).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> watchlistService.getUserWatchlist("notfound@example.com", 1, 20))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }
}
