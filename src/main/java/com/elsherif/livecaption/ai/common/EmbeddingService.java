package com.elsherif.livecaption.ai.common;

import com.elsherif.livecaption.ai.common.dto.EmbeddingResponse;

import java.util.List;

/**
 * Interface for generating text embeddings.
 * Supports multiple AI providers (OpenAI, Gemini).
 */
public interface EmbeddingService {

    /**
     * Generate embeddings for a single text
     */
    float[] embed(String text);

    /**
     * Generate embeddings for multiple texts
     */
    EmbeddingResponse embedBatch(List<String> texts);

    /**
     * Get the dimension of the embeddings produced by this service
     */
    int getEmbeddingDimension();

    /**
     * Get the provider name
     */
    AIProvider getProvider();
}
