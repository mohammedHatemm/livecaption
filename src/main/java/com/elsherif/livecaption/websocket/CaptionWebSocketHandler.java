package com.elsherif.livecaption.websocket;

import com.elsherif.livecaption.ai.common.TranscriptionService;
import com.elsherif.livecaption.ai.common.dto.TranscriptionResponse;
import com.elsherif.livecaption.ai.translation.TranslationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for real-time caption generation.
 * Receives audio chunks, transcribes them, optionally translates, and sends captions back.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CaptionWebSocketHandler extends AbstractWebSocketHandler {

    private final TranscriptionService transcriptionService;
    private final TranslationService translationService;
    private final ObjectMapper objectMapper;

    // Store session configurations
    private final Map<String, SessionConfig> sessionConfigs = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket connection established: {}", session.getId());
        sessionConfigs.put(session.getId(), new SessionConfig());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            JsonNode json = objectMapper.readTree(message.getPayload());
            String type = json.get("type").asText();

            switch (type) {
                case "config" -> handleConfig(session, json);
                case "audio" -> handleAudioMessage(session, json);
                default -> sendError(session, "Unknown message type: " + type);
            }
        } catch (Exception e) {
            log.error("Error handling message", e);
            sendError(session, e.getMessage());
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        try {
            byte[] audioData = message.getPayload().array();
            processAudio(session, audioData);
        } catch (Exception e) {
            log.error("Error handling binary message", e);
            try {
                sendError(session, e.getMessage());
            } catch (IOException ignored) {}
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket connection closed: {} - {}", session.getId(), status);
        sessionConfigs.remove(session.getId());
    }

    private void handleConfig(WebSocketSession session, JsonNode json) throws IOException {
        SessionConfig config = sessionConfigs.get(session.getId());
        if (config == null) {
            config = new SessionConfig();
            sessionConfigs.put(session.getId(), config);
        }

        if (json.has("sourceLanguage")) {
            config.sourceLanguage = json.get("sourceLanguage").asText();
        }
        if (json.has("targetLanguage")) {
            config.targetLanguage = json.get("targetLanguage").asText();
        }
        if (json.has("translate")) {
            config.translate = json.get("translate").asBoolean();
        }

        sendMessage(session, Map.of(
                "type", "config_ack",
                "sourceLanguage", config.sourceLanguage,
                "targetLanguage", config.targetLanguage,
                "translate", config.translate
        ));
    }

    private void handleAudioMessage(WebSocketSession session, JsonNode json) throws Exception {
        String audioBase64 = json.get("data").asText();
        byte[] audioData = Base64.getDecoder().decode(audioBase64);
        processAudio(session, audioData);
    }

    private void processAudio(WebSocketSession session, byte[] audioData) throws IOException {
        SessionConfig config = sessionConfigs.getOrDefault(session.getId(), new SessionConfig());

        // Transcribe the audio
        TranscriptionResponse transcription = transcriptionService.transcribeWithTimestamps(
                audioData, config.sourceLanguage
        );

        String text = transcription.getText();
        String finalText = text;

        // Translate if needed
        if (config.translate && !config.sourceLanguage.equals(config.targetLanguage)) {
            finalText = translationService.translate(text, config.sourceLanguage, config.targetLanguage);
        }

        // Send caption response
        sendMessage(session, Map.of(
                "type", "caption",
                "originalText", text,
                "text", finalText,
                "language", transcription.getLanguage() != null ? transcription.getLanguage() : config.sourceLanguage,
                "targetLanguage", config.targetLanguage,
                "segments", transcription.getSegments() != null ? transcription.getSegments() : java.util.List.of(),
                "duration", transcription.getDuration() != null ? transcription.getDuration() : 0
        ));
    }

    private void sendMessage(WebSocketSession session, Object data) throws IOException {
        if (session.isOpen()) {
            String json = objectMapper.writeValueAsString(data);
            session.sendMessage(new TextMessage(json));
        }
    }

    private void sendError(WebSocketSession session, String error) throws IOException {
        sendMessage(session, Map.of("type", "error", "message", error));
    }

    private static class SessionConfig {
        String sourceLanguage = "en";
        String targetLanguage = "ar";
        boolean translate = true;
    }
}
