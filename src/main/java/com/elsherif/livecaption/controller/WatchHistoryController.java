package com.elsherif.livecaption.controller;

import com.elsherif.livecaption.dto.request.UpdateProgressRequest;
import com.elsherif.livecaption.dto.response.PagedResponse;
import com.elsherif.livecaption.dto.response.WatchHistoryResponse;
import com.elsherif.livecaption.service.WatchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
@Tag(name = "Watch History", description = "User watch history and progress tracking endpoints")
@SecurityRequirement(name = "bearerAuth")
public class WatchHistoryController {

    private final WatchHistoryService watchHistoryService;

    @GetMapping
    @Operation(summary = "Get watch history", description = "Get the authenticated user's watch history")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Watch history returned"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<PagedResponse<WatchHistoryResponse>> getWatchHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(watchHistoryService.getWatchHistory(userDetails.getUsername(), page, size));
    }

    @GetMapping("/continue")
    @Operation(summary = "Get continue watching", description = "Get movies the user has started but not finished")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Continue watching list returned"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<List<WatchHistoryResponse>> getContinueWatching(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Maximum number of results") @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(watchHistoryService.getContinueWatching(userDetails.getUsername(), limit));
    }

    @PutMapping("/{tmdbId}")
    @Operation(summary = "Update watch progress", description = "Update the watch progress for a movie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progress updated"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Movie not found")
    })
    public ResponseEntity<WatchHistoryResponse> updateProgress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "TMDB movie ID") @PathVariable Long tmdbId,
            @Valid @RequestBody UpdateProgressRequest request) {
        return ResponseEntity.ok(watchHistoryService.updateProgress(userDetails.getUsername(), tmdbId, request));
    }

    @DeleteMapping("/{tmdbId}")
    @Operation(summary = "Delete history entry", description = "Delete a movie from the user's watch history")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "History entry deleted"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "History entry not found")
    })
    public ResponseEntity<Void> deleteHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "TMDB movie ID") @PathVariable Long tmdbId) {
        watchHistoryService.deleteHistory(userDetails.getUsername(), tmdbId);
        return ResponseEntity.noContent().build();
    }
}
