package com.elsherif.livecaption.ai.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for AI services.
 * Supports multiple providers: OpenAI, Gemini, and Groq (for Whisper).
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AIConfig {

    /**
     * The default AI provider to use (openai or gemini)
     */
    private String defaultProvider = "openai";

    /**
     * OpenAI configuration
     */
    private OpenAIConfig openai = new OpenAIConfig();

    /**
     * Google Gemini configuration
     */
    private GeminiConfig gemini = new GeminiConfig();

    /**
     * Groq configuration (for Whisper transcription)
     */
    private GroqConfig groq = new GroqConfig();

    @Data
    public static class OpenAIConfig {
        private String apiKey;
        private String baseUrl = "https://api.openai.com/v1";
        private String embeddingModel = "text-embedding-3-small";
        private String chatModel = "gpt-4o-mini";
        private String whisperModel = "whisper-1";
        private int timeout = 30000;
    }

    @Data
    public static class GeminiConfig {
        private String apiKey;
        private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";
        private String embeddingModel = "text-embedding-004";
        private String chatModel = "gemini-1.5-flash";
        private int timeout = 30000;
    }

    @Data
    public static class GroqConfig {
        private String apiKey;
        private String baseUrl = "https://api.groq.com/openai/v1";
        private String whisperModel = "whisper-large-v3";
        private int timeout = 60000;
    }

    public AIProvider getDefaultProviderEnum() {
        return AIProvider.fromValue(defaultProvider);
    }
}
