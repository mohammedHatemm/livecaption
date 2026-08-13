package com.elsherif.livecaption.controller;

import com.elsherif.livecaption.dto.response.PagedResponse;
import com.elsherif.livecaption.dto.response.WatchlistResponse;
import com.elsherif.livecaption.service.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Watchlist", description = "User watchlist management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class WatchlistController {

    private final WatchlistService watchlistService;

    @GetMapping
    @Operation(summary = "Get user watchlist", description = "Get the authenticated user's watchlist")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Watchlist returned"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<PagedResponse<WatchlistResponse>> getWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(watchlistService.getUserWatchlist(userDetails.getUsername(), page, size));
    }

    @PostMapping("/{tmdbId}")
    @Operation(summary = "Add to watchlist", description = "Add a movie to the user's watchlist")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Movie added to watchlist"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "409", description = "Movie already in watchlist")
    })
    public ResponseEntity<WatchlistResponse> addToWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "TMDB movie ID") @PathVariable Long tmdbId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(watchlistService.addToWatchlist(userDetails.getUsername(), tmdbId));
    }

    @DeleteMapping("/{tmdbId}")
    @Operation(summary = "Remove from watchlist", description = "Remove a movie from the user's watchlist")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Movie removed from watchlist"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Movie not in watchlist")
    })
    public ResponseEntity<Void> removeFromWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "TMDB movie ID") @PathVariable Long tmdbId) {
        watchlistService.removeFromWatchlist(userDetails.getUsername(), tmdbId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{tmdbId}/check")
    @Operation(summary = "Check watchlist status", description = "Check if a movie is in the user's watchlist")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Watchlist status returned"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<Map<String, Boolean>> checkWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "TMDB movie ID") @PathVariable Long tmdbId) {
        boolean inWatchlist = watchlistService.isInWatchlist(userDetails.getUsername(), tmdbId);
        return ResponseEntity.ok(Map.of("inWatchlist", inWatchlist));
    }
}
