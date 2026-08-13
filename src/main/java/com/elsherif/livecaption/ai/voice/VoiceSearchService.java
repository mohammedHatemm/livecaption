package com.elsherif.livecaption.ai.voice;

import com.elsherif.livecaption.ai.common.ChatService;
import com.elsherif.livecaption.ai.common.TranscriptionService;
import com.elsherif.livecaption.dto.response.MovieResponse;
import com.elsherif.livecaption.dto.response.PagedResponse;
import com.elsherif.livecaption.service.MovieService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Voice Search Service that processes voice commands for movie search.
 * Supports natural language queries like "action movies from 2020" or "فيلم كوميدي".
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceSearchService {

    private final TranscriptionService transcriptionService;
    private final ChatService chatService;
    private final MovieService movieService;
    private final ObjectMapper objectMapper;

    /**
     * Process voice search from audio data
     */
    public VoiceSearchResult searchByVoice(byte[] audioData, String language) {
        // Step 1: Transcribe the audio
        String transcribedText = transcriptionService.transcribe(audioData, language);
        log.info("Transcribed voice query: {}", transcribedText);

        // Step 2: Parse the search intent
        SearchIntent intent = parseSearchIntent(transcribedText);
        log.info("Parsed search intent: {}", intent);

        // Step 3: Execute the search
        PagedResponse<MovieResponse> results = executeSearch(intent);

        return VoiceSearchResult.builder()
                .transcribedText(transcribedText)
                .intent(intent)
                .results(results)
                .build();
    }

    /**
     * Process text-based voice search (for testing or pre-transcribed queries)
     */
    public VoiceSearchResult searchByText(String query) {
        SearchIntent intent = parseSearchIntent(query);
        PagedResponse<MovieResponse> results = executeSearch(intent);

        return VoiceSearchResult.builder()
                .transcribedText(query)
                .intent(intent)
                .results(results)
                .build();
    }

    private SearchIntent parseSearchIntent(String query) {
        String systemPrompt = """
            You are a movie search query parser. Analyze the user's natural language query and extract search parameters.
            
            Extract the following if mentioned:
            - searchQuery: The main search term (movie title, actor name, etc.)
            - genre: Movie genre (action, comedy, drama, horror, etc.)
            - year: Release year
            - language: Movie language preference
            - sortBy: How to sort results (popularity, rating, release_date)
            
            Respond with valid JSON only:
            {
                "searchQuery": "string or null",
                "genre": "string or null",
                "genreId": "integer or null (use TMDB genre IDs: 28=Action, 35=Comedy, 18=Drama, 27=Horror, 878=Sci-Fi, 10749=Romance, 53=Thriller, 16=Animation)",
                "year": "integer or null",
                "language": "string or null",
                "sortBy": "string or null"
            }
            """;

        try {
            String response = chatService.chat(systemPrompt, query);
            // Clean up response - remove markdown code blocks if present
            response = response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            
            JsonNode json = objectMapper.readTree(response);

            return SearchIntent.builder()
                    .originalQuery(query)
                    .searchQuery(getTextOrNull(json, "searchQuery"))
                    .genre(getTextOrNull(json, "genre"))
                    .genreId(getIntOrNull(json, "genreId"))
                    .year(getIntOrNull(json, "year"))
                    .language(getTextOrNull(json, "language"))
                    .sortBy(getTextOrNull(json, "sortBy"))
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse search intent", e);
            // Fallback to simple text search
            return SearchIntent.builder()
                    .originalQuery(query)
                    .searchQuery(query)
                    .build();
        }
    }

    private PagedResponse<MovieResponse> executeSearch(SearchIntent intent) {
        // If we have a specific search query, use text search
        if (intent.getSearchQuery() != null && !intent.getSearchQuery().isBlank()) {
            return movieService.searchMovies(intent.getSearchQuery(), 1);
        }

        // Otherwise, use discover with filters
        return movieService.discoverMovies(intent.getGenreId(), intent.getYear(), 1);
    }

    private String getTextOrNull(JsonNode json, String field) {
        return json.has(field) && !json.get(field).isNull() ? json.get(field).asText() : null;
    }

    private Integer getIntOrNull(JsonNode json, String field) {
        return json.has(field) && !json.get(field).isNull() ? json.get(field).asInt() : null;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VoiceSearchResult {
        private String transcribedText;
        private SearchIntent intent;
        private PagedResponse<MovieResponse> results;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SearchIntent {
        private String originalQuery;
        private String searchQuery;
        private String genre;
        private Integer genreId;
        private Integer year;
        private String language;
        private String sortBy;
    }
}
