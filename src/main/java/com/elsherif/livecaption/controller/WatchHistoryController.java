package com.elsherif.livecaption.controller;

import com.elsherif.livecaption.dto.request.UpdateProgressRequest;
import com.elsherif.livecaption.dto.response.PagedResponse;
import com.elsherif.livecaption.dto.response.WatchHistoryResponse;
import com.elsherif.livecaption.service.WatchHistoryService;
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
public class WatchHistoryController {

    private final WatchHistoryService watchHistoryService;

    @GetMapping
    public ResponseEntity<PagedResponse<WatchHistoryResponse>> getWatchHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(watchHistoryService.getWatchHistory(userDetails.getUsername(), page, size));
    }

    @GetMapping("/continue")
    public ResponseEntity<List<WatchHistoryResponse>> getContinueWatching(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(watchHistoryService.getContinueWatching(userDetails.getUsername(), limit));
    }

    @PutMapping("/{tmdbId}")
    public ResponseEntity<WatchHistoryResponse> updateProgress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long tmdbId,
            @Valid @RequestBody UpdateProgressRequest request) {
        return ResponseEntity.ok(watchHistoryService.updateProgress(userDetails.getUsername(), tmdbId, request));
    }

    @DeleteMapping("/{tmdbId}")
    public ResponseEntity<Void> deleteHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long tmdbId) {
        watchHistoryService.deleteHistory(userDetails.getUsername(), tmdbId);
        return ResponseEntity.noContent().build();
    }
}
