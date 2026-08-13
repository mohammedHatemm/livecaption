package com.elsherif.livecaption.ai.transcription;

import com.elsherif.livecaption.ai.common.AIConfig;
import com.elsherif.livecaption.ai.common.TranscriptionService;
import com.elsherif.livecaption.ai.common.dto.TranscriptionRequest;
import com.elsherif.livecaption.ai.common.dto.TranscriptionResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Transcription service using Groq's Whisper API.
 * Groq provides fast and affordable Whisper transcription.
 */
@Slf4j
@Service
public class GroqTranscriptionService implements TranscriptionService {

    private final WebClient webClient;
    private final AIConfig.GroqConfig config;
    private final ObjectMapper objectMapper;

    public GroqTranscriptionService(AIConfig aiConfig, ObjectMapper objectMapper) {
        this.config = aiConfig.getGroq();
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.getApiKey())
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024)) // 50MB
                .build();
    }

    @Override
    public TranscriptionResponse transcribe(TranscriptionRequest request) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        
        builder.part("file", new ByteArrayResource(request.getAudioData()) {
            @Override
            public String getFilename() {
                return request.getFileName() != null ? request.getFileName() : "audio.mp3";
            }
        }).contentType(MediaType.APPLICATION_OCTET_STREAM);
        
        builder.part("model", config.getWhisperModel());
        
        if (request.getLanguage() != null) {
            builder.part("language", request.getLanguage());
        }
        
        String responseFormat = request.getResponseFormat() != null ? request.getResponseFormat() : "verbose_json";
        builder.part("response_format", responseFormat);

        if (Boolean.TRUE.equals(request.getTimestamps())) {
            builder.part("timestamp_granularities[]", "word");
            builder.part("timestamp_granularities[]", "segment");
        }

        try {
            String responseJson = webClient.post()
                    .uri("/audio/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseTranscriptionResponse(responseJson);

        } catch (Exception e) {
            log.error("Failed to transcribe audio with Groq", e);
            throw new RuntimeException("Failed to transcribe audio", e);
        }
    }

    @Override
    public String transcribe(byte[] audioData, String language) {
        var request = TranscriptionRequest.builder()
                .audioData(audioData)
                .language(language)
                .responseFormat("text")
                .build();

        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(audioData) {
                @Override
                public String getFilename() {
                    return "audio.mp3";
                }
            }).contentType(MediaType.APPLICATION_OCTET_STREAM);
            builder.part("model", config.getWhisperModel());
            if (language != null) {
                builder.part("language", language);
            }
            builder.part("response_format", "text");

            return webClient.post()
                    .uri("/audio/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        } catch (Exception e) {
            log.error("Failed to transcribe audio with Groq", e);
            throw new RuntimeException("Failed to transcribe audio", e);
        }
    }

    @Override
    public TranscriptionResponse transcribeWithTimestamps(byte[] audioData, String language) {
        var request = TranscriptionRequest.builder()
                .audioData(audioData)
                .language(language)
                .responseFormat("verbose_json")
                .timestamps(true)
                .build();
        return transcribe(request);
    }

    @Override
    public String getProviderName() {
        return "Groq Whisper";
    }

    private TranscriptionResponse parseTranscriptionResponse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);

            String text = root.has("text") ? root.get("text").asText() : "";
            String language = root.has("language") ? root.get("language").asText() : null;
            Double duration = root.has("duration") ? root.get("duration").asDouble() : null;

            List<TranscriptionResponse.Segment> segments = new ArrayList<>();
            if (root.has("segments")) {
                for (JsonNode segmentNode : root.get("segments")) {
                    List<TranscriptionResponse.Word> words = new ArrayList<>();
                    if (segmentNode.has("words")) {
                        for (JsonNode wordNode : segmentNode.get("words")) {
                            words.add(TranscriptionResponse.Word.builder()
                                    .word(wordNode.get("word").asText())
                                    .start(wordNode.get("start").asDouble())
                                    .end(wordNode.get("end").asDouble())
                                    .build());
                        }
                    }

                    segments.add(TranscriptionResponse.Segment.builder()
                            .id(segmentNode.get("id").asInt())
                            .start(segmentNode.get("start").asDouble())
                            .end(segmentNode.get("end").asDouble())
                            .text(segmentNode.get("text").asText())
                            .words(words)
                            .build());
                }
            }

            return TranscriptionResponse.builder()
                    .text(text)
                    .language(language)
                    .duration(duration)
                    .segments(segments)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse transcription response", e);
            throw new RuntimeException("Failed to parse transcription response", e);
        }
    }
}
