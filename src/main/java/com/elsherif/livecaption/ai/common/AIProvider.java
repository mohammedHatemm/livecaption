package com.elsherif.livecaption.ai.common;

/**
 * Supported AI providers for the application.
 * Users can choose between OpenAI and Gemini for various AI features.
 */
public enum AIProvider {
    OPENAI("openai"),
    GEMINI("gemini");

    private final String value;

    AIProvider(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AIProvider fromValue(String value) {
        for (AIProvider provider : values()) {
            if (provider.value.equalsIgnoreCase(value)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown AI provider: " + value);
    }
}
