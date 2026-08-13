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
public class WatchlistResponse {
    private Long id;
    private Long tmdbId;
    private MovieResponse movie;
    private LocalDateTime addedAt;
}
