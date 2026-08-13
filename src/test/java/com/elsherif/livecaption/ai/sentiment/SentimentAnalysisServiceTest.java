package com.elsherif.livecaption.ai.sentiment;

import com.elsherif.livecaption.ai.common.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SentimentAnalysisServiceTest {

    @Mock
    private ChatService chatService;

    private SentimentAnalysisService sentimentAnalysisService;

    @BeforeEach
    void setUp() {
        sentimentAnalysisService = new SentimentAnalysisService(chatService, new ObjectMapper());
    }

    @Test
    void analyzeSentiment_shouldReturnPositiveSentiment() {
        String jsonResponse = """
            {
                "sentiment": "positive",
                "score": 0.8,
                "confidence": 0.95,
                "aspects": {
                    "acting": "positive",
                    "plot": "positive"
                }
            }
            """;
        when(chatService.chat(anyString(), anyString())).thenReturn(jsonResponse);

        var result = sentimentAnalysisService.analyzeSentiment("This movie was amazing! Great acting and plot.");

        assertThat(result.getSentiment()).isEqualTo("positive");
        assertThat(result.getScore()).isEqualTo(0.8);
        assertThat(result.getConfidence()).isEqualTo(0.95);
        assertThat(result.getAspects()).containsKey("acting");
    }

    @Test
    void analyzeSentiment_shouldReturnNegativeSentiment() {
        String jsonResponse = """
            {
                "sentiment": "negative",
                "score": -0.7,
                "confidence": 0.9,
                "aspects": {
                    "plot": "negative"
                }
            }
            """;
        when(chatService.chat(anyString(), anyString())).thenReturn(jsonResponse);

        var result = sentimentAnalysisService.analyzeSentiment("Terrible movie. Waste of time.");

        assertThat(result.getSentiment()).isEqualTo("negative");
        assertThat(result.getScore()).isLessThan(0);
    }

    @Test
    void analyzeSentiment_shouldHandleParsingErrors() {
        when(chatService.chat(anyString(), anyString())).thenReturn("invalid json");

        var result = sentimentAnalysisService.analyzeSentiment("Some review");

        assertThat(result.getSentiment()).isEqualTo("neutral");
        assertThat(result.getScore()).isEqualTo(0.0);
    }

    @Test
    void analyzeReviews_shouldReturnAggregateInsights() {
        String jsonResponse = """
            {
                "overallSentiment": "positive",
                "averageScore": 0.6,
                "positivePercentage": 70,
                "negativePercentage": 20,
                "neutralPercentage": 10,
                "pros": ["Great acting", "Good visuals"],
                "cons": ["Slow pacing"],
                "summary": "Generally well received",
                "commonThemes": ["action", "adventure"]
            }
            """;
        when(chatService.chat(anyString(), anyString())).thenReturn(jsonResponse);

        var result = sentimentAnalysisService.analyzeReviews(
                List.of("Great movie!", "Loved it", "Could be better"),
                "Test Movie"
        );

        assertThat(result.getOverallSentiment()).isEqualTo("positive");
        assertThat(result.getPositivePercentage()).isEqualTo(70);
        assertThat(result.getPros()).contains("Great acting");
        assertThat(result.getCons()).contains("Slow pacing");
    }

    @Test
    void analyzeReviews_shouldHandleEmptyReviews() {
        var result = sentimentAnalysisService.analyzeReviews(List.of(), "Test Movie");

        assertThat(result.getTotalReviews()).isEqualTo(0);
        assertThat(result.getMovieTitle()).isEqualTo("Test Movie");
    }

    @Test
    void summarizeReview_shouldReturnConciseSummary() {
        when(chatService.chat(anyString(), anyString())).thenReturn("A great action movie with solid performances.");

        String result = sentimentAnalysisService.summarizeReview(
                "This is a long review about the movie...", 50
        );

        assertThat(result).isEqualTo("A great action movie with solid performances.");
    }
}
