package com.elsherif.livecaption.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchHistoryResponse {
    private Long id;
    private Long tmdbId;
    private MovieResponse movie;
    private Integer progressSeconds;
    private Integer durationSeconds;
    private Integer progressPercentage;
    private Boolean completed;
    private LocalDateTime lastWatchedAt;
}
