package com.elsherif.livecaption.controller;

import com.elsherif.livecaption.dto.response.PagedResponse;
import com.elsherif.livecaption.dto.response.WatchlistResponse;
import com.elsherif.livecaption.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @GetMapping
    public ResponseEntity<PagedResponse<WatchlistResponse>> getWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(watchlistService.getUserWatchlist(userDetails.getUsername(), page, size));
    }

    @PostMapping("/{tmdbId}")
    public ResponseEntity<WatchlistResponse> addToWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long tmdbId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(watchlistService.addToWatchlist(userDetails.getUsername(), tmdbId));
    }

    @DeleteMapping("/{tmdbId}")
    public ResponseEntity<Void> removeFromWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long tmdbId) {
        watchlistService.removeFromWatchlist(userDetails.getUsername(), tmdbId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{tmdbId}/check")
    public ResponseEntity<Map<String, Boolean>> checkWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long tmdbId) {
        boolean inWatchlist = watchlistService.isInWatchlist(userDetails.getUsername(), tmdbId);
        return ResponseEntity.ok(Map.of("inWatchlist", inWatchlist));
    }
}
