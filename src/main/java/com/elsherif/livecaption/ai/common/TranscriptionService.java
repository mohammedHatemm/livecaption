package com.elsherif.livecaption.ai.common;

import com.elsherif.livecaption.ai.common.dto.TranscriptionRequest;
import com.elsherif.livecaption.ai.common.dto.TranscriptionResponse;

/**
 * Interface for audio transcription (Speech-to-Text).
 * Primarily uses Whisper via OpenAI or Groq.
 */
public interface TranscriptionService {

    /**
     * Transcribe audio data to text
     */
    TranscriptionResponse transcribe(TranscriptionRequest request);

    /**
     * Transcribe audio file to text (simple version)
     */
    String transcribe(byte[] audioData, String language);

    /**
     * Transcribe audio with timestamps for subtitle generation
     */
    TranscriptionResponse transcribeWithTimestamps(byte[] audioData, String language);

    /**
     * Get the provider name (OpenAI or Groq)
     */
    String getProviderName();
}
