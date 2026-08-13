package com.elsherif.livecaption.ai.openai;

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
@ConditionalOnProperty(name = "ai.default-provider", havingValue = "openai", matchIfMissing = true)
public class OpenAIEmbeddingService implements EmbeddingService {

    private final WebClient webClient;
    private final AIConfig.OpenAIConfig config;
    private final ObjectMapper objectMapper;

    private static final int EMBEDDING_DIMENSION = 1536; // text-embedding-3-small

    public OpenAIEmbeddingService(AIConfig aiConfig, ObjectMapper objectMapper) {
        this.config = aiConfig.getOpenai();
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.getApiKey())
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
        var requestBody = Map.of(
                "input", texts,
                "model", config.getEmbeddingModel()
        );

        try {
            String responseJson = webClient.post()
                    .uri("/embeddings")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode dataArray = root.get("data");

            List<float[]> embeddings = new ArrayList<>();
            for (JsonNode item : dataArray) {
                JsonNode embeddingArray = item.get("embedding");
                float[] embedding = new float[embeddingArray.size()];
                for (int i = 0; i < embeddingArray.size(); i++) {
                    embedding[i] = (float) embeddingArray.get(i).asDouble();
                }
                embeddings.add(embedding);
            }

            int totalTokens = root.get("usage").get("total_tokens").asInt();

            return EmbeddingResponse.builder()
                    .embeddings(embeddings)
                    .totalTokens(totalTokens)
                    .model(config.getEmbeddingModel())
                    .build();

        } catch (Exception e) {
            log.error("Failed to generate embeddings with OpenAI", e);
            throw new RuntimeException("Failed to generate embeddings", e);
        }
    }

    @Override
    public int getEmbeddingDimension() {
        return EMBEDDING_DIMENSION;
    }

    @Override
    public AIProvider getProvider() {
        return AIProvider.OPENAI;
    }
}
