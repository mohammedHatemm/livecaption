package com.elsherif.livecaption.ai.sentiment;

import com.elsherif.livecaption.ai.common.ChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Sentiment Analysis Service for movie reviews.
 * Provides:
 * - Sentiment classification (positive, negative, neutral)
 * - Review summarization
 * - Pros/cons extraction
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SentimentAnalysisService {

    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    /**
     * Analyze sentiment of a single review
     */
    public SentimentResult analyzeSentiment(String reviewText) {
        String systemPrompt = """
            You are a sentiment analysis expert for movie reviews.
            Analyze the given review and provide:
            1. sentiment: "positive", "negative", or "neutral"
            2. score: A number from -1.0 (very negative) to 1.0 (very positive)
            3. confidence: A number from 0.0 to 1.0 indicating confidence
            4. aspects: Key aspects mentioned (acting, plot, visuals, etc.)
            
            Respond with valid JSON only:
            {
                "sentiment": "string",
                "score": number,
                "confidence": number,
                "aspects": {
                    "aspect_name": "positive|negative|neutral"
                }
            }
            """;

        try {
            String response = chatService.chat(systemPrompt, reviewText);
            response = cleanJsonResponse(response);
            
            JsonNode json = objectMapper.readTree(response);

            var aspectsBuilder = new java.util.HashMap<String, String>();
            JsonNode aspects = json.get("aspects");
            if (aspects != null && aspects.isObject()) {
                aspects.fields().forEachRemaining(entry -> 
                    aspectsBuilder.put(entry.getKey(), entry.getValue().asText()));
            }

            return SentimentResult.builder()
                    .originalText(reviewText)
                    .sentiment(json.get("sentiment").asText())
                    .score(json.get("score").asDouble())
                    .confidence(json.get("confidence").asDouble())
                    .aspects(aspectsBuilder)
                    .build();

        } catch (Exception e) {
            log.error("Failed to analyze sentiment", e);
            return SentimentResult.builder()
                    .originalText(reviewText)
                    .sentiment("neutral")
                    .score(0.0)
                    .confidence(0.0)
                    .build();
        }
    }

    /**
     * Analyze multiple reviews and provide aggregate insights
     */
    public ReviewInsights analyzeReviews(List<String> reviews, String movieTitle) {
        if (reviews.isEmpty()) {
            return ReviewInsights.builder()
                    .movieTitle(movieTitle)
                    .totalReviews(0)
                    .build();
        }

        // Combine reviews for batch analysis
        String combinedReviews = String.join("\n---\n", reviews);

        String systemPrompt = """
            You are a movie review analyst. Analyze the following reviews and provide comprehensive insights.
            
            Respond with valid JSON:
            {
                "overallSentiment": "positive|negative|mixed",
                "averageScore": number (-1.0 to 1.0),
                "positivePercentage": number (0-100),
                "negativePercentage": number (0-100),
                "neutralPercentage": number (0-100),
                "pros": ["list of positive points"],
                "cons": ["list of negative points"],
                "summary": "Brief summary of overall reception",
                "commonThemes": ["recurring themes in reviews"]
            }
            """;

        String userPrompt = String.format("Movie: %s\n\nReviews:\n%s", movieTitle, combinedReviews);

        try {
            String response = chatService.chat(systemPrompt, userPrompt);
            response = cleanJsonResponse(response);
            
            JsonNode json = objectMapper.readTree(response);

            return ReviewInsights.builder()
                    .movieTitle(movieTitle)
                    .totalReviews(reviews.size())
                    .overallSentiment(json.get("overallSentiment").asText())
                    .averageScore(json.get("averageScore").asDouble())
                    .positivePercentage(json.get("positivePercentage").asInt())
                    .negativePercentage(json.get("negativePercentage").asInt())
                    .neutralPercentage(json.get("neutralPercentage").asInt())
                    .pros(jsonArrayToList(json.get("pros")))
                    .cons(jsonArrayToList(json.get("cons")))
                    .summary(json.get("summary").asText())
                    .commonThemes(jsonArrayToList(json.get("commonThemes")))
                    .build();

        } catch (Exception e) {
            log.error("Failed to analyze reviews", e);
            return ReviewInsights.builder()
                    .movieTitle(movieTitle)
                    .totalReviews(reviews.size())
                    .overallSentiment("unknown")
                    .build();
        }
    }

    /**
     * Generate a concise summary of a review
     */
    public String summarizeReview(String reviewText, int maxWords) {
        String systemPrompt = String.format("""
            Summarize the following movie review in %d words or less.
            Keep the key opinion and main points. Be concise.
            Only respond with the summary, nothing else.
            """, maxWords);

        return chatService.chat(systemPrompt, reviewText).trim();
    }

    private String cleanJsonResponse(String response) {
        return response.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
    }

    private List<String> jsonArrayToList(JsonNode arrayNode) {
        if (arrayNode == null || !arrayNode.isArray()) {
            return List.of();
        }
        var list = new java.util.ArrayList<String>();
        arrayNode.forEach(node -> list.add(node.asText()));
        return list;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SentimentResult {
        private String originalText;
        private String sentiment;
        private double score;
        private double confidence;
        private java.util.Map<String, String> aspects;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReviewInsights {
        private String movieTitle;
        private int totalReviews;
        private String overallSentiment;
        private double averageScore;
        private int positivePercentage;
        private int negativePercentage;
        private int neutralPercentage;
        private List<String> pros;
        private List<String> cons;
        private String summary;
        private List<String> commonThemes;
    }
}
