package com.elsherif.livecaption.ai.common;

import com.elsherif.livecaption.ai.common.dto.ChatRequest;
import com.elsherif.livecaption.ai.common.dto.ChatResponse;

import java.util.List;

/**
 * Interface for chat completions.
 * Supports multiple AI providers (OpenAI, Gemini).
 */
public interface ChatService {

    /**
     * Send a simple message and get a response
     */
    String chat(String message);

    /**
     * Send a message with system prompt
     */
    String chat(String systemPrompt, String userMessage);

    /**
     * Send a full chat request with message history
     */
    ChatResponse chat(ChatRequest request);

    /**
     * Generate a structured response (JSON) based on a prompt
     */
    String generateStructured(String systemPrompt, String userMessage, String jsonSchema);

    /**
     * Get the provider name
     */
    AIProvider getProvider();
}
