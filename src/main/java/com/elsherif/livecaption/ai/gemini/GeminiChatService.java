package com.elsherif.livecaption.ai.gemini;

import com.elsherif.livecaption.ai.common.AIConfig;
import com.elsherif.livecaption.ai.common.AIProvider;
import com.elsherif.livecaption.ai.common.ChatService;
import com.elsherif.livecaption.ai.common.dto.ChatRequest;
import com.elsherif.livecaption.ai.common.dto.ChatResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "ai.default-provider", havingValue = "gemini")
public class GeminiChatService implements ChatService {

    private final WebClient webClient;
    private final AIConfig.GeminiConfig config;
    private final ObjectMapper objectMapper;

    public GeminiChatService(AIConfig aiConfig, ObjectMapper objectMapper) {
        this.config = aiConfig.getGemini();
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String chat(String message) {
        return chat("You are a helpful assistant.", message);
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        var request = ChatRequest.builder()
                .messages(List.of(
                        ChatRequest.Message.builder().role("system").content(systemPrompt).build(),
                        ChatRequest.Message.builder().role("user").content(userMessage).build()
                ))
                .build();
        return chat(request).getContent();
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        List<Map<String, Object>> contents = new ArrayList<>();
        String systemInstruction = null;

        for (var msg : request.getMessages()) {
            if ("system".equals(msg.getRole())) {
                systemInstruction = msg.getContent();
            } else {
                String role = "user".equals(msg.getRole()) ? "user" : "model";
                contents.add(Map.of(
                        "role", role,
                        "parts", List.of(Map.of("text", msg.getContent()))
                ));
            }
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", contents);

        if (systemInstruction != null) {
            requestBody.put("systemInstruction", Map.of(
                    "parts", List.of(Map.of("text", systemInstruction))
            ));
        }

        Map<String, Object> generationConfig = new HashMap<>();
        if (request.getTemperature() != null) {
            generationConfig.put("temperature", request.getTemperature());
        }
        if (request.getMaxTokens() != null) {
            generationConfig.put("maxOutputTokens", request.getMaxTokens());
        }
        if (!generationConfig.isEmpty()) {
            requestBody.put("generationConfig", generationConfig);
        }

        String model = request.getModel() != null ? request.getModel() : config.getChatModel();

        try {
            String responseJson = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/{model}:generateContent")
                            .queryParam("key", config.getApiKey())
                            .build(model))
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode candidate = root.get("candidates").get(0);
            String content = candidate.get("content").get("parts").get(0).get("text").asText();
            String finishReason = candidate.get("finishReason").asText();

            JsonNode usageMetadata = root.get("usageMetadata");
            int promptTokens = usageMetadata != null ? usageMetadata.get("promptTokenCount").asInt() : 0;
            int completionTokens = usageMetadata != null ? usageMetadata.get("candidatesTokenCount").asInt() : 0;

            return ChatResponse.builder()
                    .content(content)
                    .finishReason(finishReason)
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(promptTokens + completionTokens)
                    .model(model)
                    .build();

        } catch (Exception e) {
            log.error("Failed to chat with Gemini", e);
            throw new RuntimeException("Failed to chat with Gemini", e);
        }
    }

    @Override
    public String generateStructured(String systemPrompt, String userMessage, String jsonSchema) {
        String fullSystemPrompt = systemPrompt + "\n\nRespond with valid JSON matching this schema:\n" + jsonSchema;
        return chat(fullSystemPrompt, userMessage);
    }

    @Override
    public AIProvider getProvider() {
        return AIProvider.GEMINI;
    }
}
