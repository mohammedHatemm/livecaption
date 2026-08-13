package com.elsherif.livecaption.ai.openai;

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
@ConditionalOnProperty(name = "ai.default-provider", havingValue = "openai", matchIfMissing = true)
public class OpenAIChatService implements ChatService {

    private final WebClient webClient;
    private final AIConfig.OpenAIConfig config;
    private final ObjectMapper objectMapper;

    public OpenAIChatService(AIConfig aiConfig, ObjectMapper objectMapper) {
        this.config = aiConfig.getOpenai();
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.getApiKey())
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
        List<Map<String, String>> messages = new ArrayList<>();
        for (var msg : request.getMessages()) {
            messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", request.getModel() != null ? request.getModel() : config.getChatModel());
        requestBody.put("messages", messages);

        if (request.getTemperature() != null) {
            requestBody.put("temperature", request.getTemperature());
        }
        if (request.getMaxTokens() != null) {
            requestBody.put("max_tokens", request.getMaxTokens());
        }

        try {
            String responseJson = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode choice = root.get("choices").get(0);
            JsonNode usage = root.get("usage");

            return ChatResponse.builder()
                    .content(choice.get("message").get("content").asText())
                    .finishReason(choice.get("finish_reason").asText())
                    .promptTokens(usage.get("prompt_tokens").asInt())
                    .completionTokens(usage.get("completion_tokens").asInt())
                    .totalTokens(usage.get("total_tokens").asInt())
                    .model(root.get("model").asText())
                    .build();

        } catch (Exception e) {
            log.error("Failed to chat with OpenAI", e);
            throw new RuntimeException("Failed to chat with OpenAI", e);
        }
    }

    @Override
    public String generateStructured(String systemPrompt, String userMessage, String jsonSchema) {
        String fullSystemPrompt = systemPrompt + "\n\nRespond with valid JSON matching this schema:\n" + jsonSchema;
        return chat(fullSystemPrompt, userMessage);
    }

    @Override
    public AIProvider getProvider() {
        return AIProvider.OPENAI;
    }
}
