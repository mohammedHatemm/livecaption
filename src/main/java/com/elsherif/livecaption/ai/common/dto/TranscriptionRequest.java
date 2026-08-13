package com.elsherif.livecaption.ai.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionRequest {
    private byte[] audioData;
    private String fileName;
    private String language; // Optional: ISO 639-1 code (e.g., "en", "ar")
    private String responseFormat; // json, text, srt, verbose_json, vtt
    private Boolean timestamps; // Include word-level timestamps
}
