package com.elsherif.livecaption.ai.gemini;

import com.elsherif.livecaption.ai.common.AIConfig;
import com.elsherif.livecaption.ai.common.AIProvider;
import com.elsherif.livecaption.ai.common.EmbeddingService;
import com.elsherif.livecaption.ai.common.dto.EmbeddingResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "ai.default-provider", havingValue = "gemini")
public class GeminiEmbeddingService implements EmbeddingService {

    private final WebClient webClient;
    private final AIConfig.GeminiConfig config;
    private final ObjectMapper objectMapper;

    private static final int EMBEDDING_DIMENSION = 768; // text-embedding-004

    public GeminiEmbeddingService(AIConfig aiConfig, ObjectMapper objectMapper) {
        this.config = aiConfig.getGemini();
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public float[] embed(String text) {
        var response = embedBatch(List.of(text));
        return response.getEmbeddings().getFirst();
    }

    @Override
    public EmbeddingResponse embedBatch(List<String> texts) {
        List<float[]> allEmbeddings = new ArrayList<>();

        for (String text : texts) {
            var requestBody = Map.of(
                    "model", "models/" + config.getEmbeddingModel(),
                    "content", Map.of("parts", List.of(Map.of("text", text)))
            );

            try {
                String responseJson = webClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/models/{model}:embedContent")
                                .queryParam("key", config.getApiKey())
                                .build(config.getEmbeddingModel()))
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                JsonNode root = objectMapper.readTree(responseJson);
                JsonNode embeddingArray = root.get("embedding").get("values");

                float[] embedding = new float[embeddingArray.size()];
                for (int i = 0; i < embeddingArray.size(); i++) {
                    embedding[i] = (float) embeddingArray.get(i).asDouble();
                }
                allEmbeddings.add(embedding);

            } catch (Exception e) {
                log.error("Failed to generate embedding with Gemini for text: {}", text.substring(0, Math.min(50, text.length())), e);
                throw new RuntimeException("Failed to generate embeddings", e);
            }
        }

        return EmbeddingResponse.builder()
                .embeddings(allEmbeddings)
                .totalTokens(0) // Gemini doesn't return token count for embeddings
                .model(config.getEmbeddingModel())
                .build();
    }

    @Override
    public int getEmbeddingDimension() {
        return EMBEDDING_DIMENSION;
    }

    @Override
    public AIProvider getProvider() {
        return AIProvider.GEMINI;
    }
}
