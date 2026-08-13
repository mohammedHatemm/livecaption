package com.elsherif.livecaption.controller;

import com.elsherif.livecaption.ai.recommender.RecommendationService;
import com.elsherif.livecaption.ai.sentiment.SentimentAnalysisService;
import com.elsherif.livecaption.ai.translation.TranslationService;
import com.elsherif.livecaption.ai.voice.VoiceSearchService;
import com.elsherif.livecaption.dto.response.MovieResponse;
import com.elsherif.livecaption.entity.User;
import com.elsherif.livecaption.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Services", description = "AI-powered features: voice search, recommendations, sentiment analysis")
public class AIController {

    private final VoiceSearchService voiceSearchService;
    private final RecommendationService recommendationService;
    private final SentimentAnalysisService sentimentAnalysisService;
    private final TranslationService translationService;
    private final UserRepository userRepository;

    // ==================== Voice Search ====================

    @PostMapping(value = "/voice-search", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Voice search", description = "Search movies using voice command")
    public ResponseEntity<VoiceSearchService.VoiceSearchResult> voiceSearch(
            @Parameter(description = "Audio file (mp3, wav, etc.)") @RequestParam("audio") MultipartFile audioFile,
            @Parameter(description = "Audio language (en, ar)") @RequestParam(defaultValue = "en") String language) {
        try {
            byte[] audioData = audioFile.getBytes();
            var result = voiceSearchService.searchByVoice(audioData, language);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process voice search", e);
        }
    }

    @PostMapping("/voice-search/text")
    @Operation(summary = "Text-based voice search", description = "Search movies using natural language text query")
    public ResponseEntity<VoiceSearchService.VoiceSearchResult> textSearch(
            @RequestBody Map<String, String> request) {
        String query = request.get("query");
        var result = voiceSearchService.searchByText(query);
        return ResponseEntity.ok(result);
    }

    // ==================== Recommendations ====================

    @GetMapping("/recommendations")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get personalized recommendations", description = "Get AI-powered movie recommendations based on watch history")
    public ResponseEntity<List<MovieResponse>> getRecommendations(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Number of recommendations") @RequestParam(defaultValue = "10") int limit) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        var recommendations = recommendationService.getPersonalizedRecommendations(user.getId(), limit);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/recommendations/similar/{tmdbId}")
    @Operation(summary = "Get similar movies", description = "Get movies similar to a specific movie")
    public ResponseEntity<List<MovieResponse>> getSimilarMovies(
            @Parameter(description = "TMDB movie ID") @PathVariable Long tmdbId,
            @Parameter(description = "Number of results") @RequestParam(defaultValue = "10") int limit) {
        var similar = recommendationService.getSimilarMovies(tmdbId, limit);
        return ResponseEntity.ok(similar);
    }

    // ==================== Sentiment Analysis ====================

    @PostMapping("/sentiment/analyze")
    @Operation(summary = "Analyze review sentiment", description = "Analyze the sentiment of a movie review")
    public ResponseEntity<SentimentAnalysisService.SentimentResult> analyzeSentiment(
            @RequestBody Map<String, String> request) {
        String reviewText = request.get("review");
        var result = sentimentAnalysisService.analyzeSentiment(reviewText);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/sentiment/insights")
    @Operation(summary = "Get review insights", description = "Analyze multiple reviews and get aggregate insights")
    public ResponseEntity<SentimentAnalysisService.ReviewInsights> getReviewInsights(
            @RequestBody ReviewInsightsRequest request) {
        var insights = sentimentAnalysisService.analyzeReviews(request.reviews(), request.movieTitle());
        return ResponseEntity.ok(insights);
    }

    @PostMapping("/sentiment/summarize")
    @Operation(summary = "Summarize review", description = "Generate a concise summary of a movie review")
    public ResponseEntity<Map<String, String>> summarizeReview(
            @RequestBody Map<String, Object> request) {
        String reviewText = (String) request.get("review");
        int maxWords = request.containsKey("maxWords") ? (Integer) request.get("maxWords") : 50;
        String summary = sentimentAnalysisService.summarizeReview(reviewText, maxWords);
        return ResponseEntity.ok(Map.of("summary", summary));
    }

    // ==================== Translation ====================

    @PostMapping("/translate")
    @Operation(summary = "Translate text", description = "Translate text between languages")
    public ResponseEntity<Map<String, String>> translate(
            @RequestBody TranslateRequest request) {
        String translated = translationService.translate(
                request.text(), request.sourceLanguage(), request.targetLanguage());
        return ResponseEntity.ok(Map.of(
                "originalText", request.text(),
                "translatedText", translated,
                "sourceLanguage", request.sourceLanguage(),
                "targetLanguage", request.targetLanguage()
        ));
    }

    @GetMapping("/translate/languages")
    @Operation(summary = "Get supported languages", description = "Get list of supported translation languages")
    public ResponseEntity<Map<String, String>> getSupportedLanguages() {
        return ResponseEntity.ok(translationService.getSupportedLanguages());
    }

    @PostMapping("/translate/detect")
    @Operation(summary = "Detect language", description = "Detect the language of the given text")
    public ResponseEntity<Map<String, String>> detectLanguage(
            @RequestBody Map<String, String> request) {
        String text = request.get("text");
        String language = translationService.detectLanguage(text);
        return ResponseEntity.ok(Map.of("language", language));
    }

    // ==================== Request DTOs ====================

    public record ReviewInsightsRequest(String movieTitle, List<String> reviews) {}
    public record TranslateRequest(String text, String sourceLanguage, String targetLanguage) {}
}
