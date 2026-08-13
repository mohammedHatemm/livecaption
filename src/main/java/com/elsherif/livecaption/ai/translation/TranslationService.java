package com.elsherif.livecaption.ai.translation;

import com.elsherif.livecaption.ai.common.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Translation service using AI (OpenAI/Gemini).
 * Supports multiple languages and context-aware translation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    private final ChatService chatService;

    private static final Map<String, String> LANGUAGE_NAMES = Map.ofEntries(
            Map.entry("en", "English"),
            Map.entry("ar", "Arabic"),
            Map.entry("es", "Spanish"),
            Map.entry("fr", "French"),
            Map.entry("de", "German"),
            Map.entry("it", "Italian"),
            Map.entry("pt", "Portuguese"),
            Map.entry("ru", "Russian"),
            Map.entry("zh", "Chinese"),
            Map.entry("ja", "Japanese"),
            Map.entry("ko", "Korean"),
            Map.entry("hi", "Hindi"),
            Map.entry("tr", "Turkish")
    );

    /**
     * Translate text from source language to target language
     */
    public String translate(String text, String sourceLanguage, String targetLanguage) {
        String sourceLang = LANGUAGE_NAMES.getOrDefault(sourceLanguage, sourceLanguage);
        String targetLang = LANGUAGE_NAMES.getOrDefault(targetLanguage, targetLanguage);

        String systemPrompt = """
            You are a professional translator. Translate the given text accurately while:
            1. Preserving the original meaning and tone
            2. Using natural expressions in the target language
            3. Keeping any technical terms or proper nouns as appropriate
            
            Only respond with the translated text, nothing else.
            """;

        String userPrompt = String.format(
                "Translate from %s to %s:\n\n%s",
                sourceLang, targetLang, text
        );

        return chatService.chat(systemPrompt, userPrompt).trim();
    }

    /**
     * Translate subtitle/caption text with timing context
     */
    public String translateSubtitle(String text, String sourceLanguage, String targetLanguage, String previousContext) {
        String sourceLang = LANGUAGE_NAMES.getOrDefault(sourceLanguage, sourceLanguage);
        String targetLang = LANGUAGE_NAMES.getOrDefault(targetLanguage, targetLanguage);

        String systemPrompt = """
            You are a professional subtitle translator. Translate the given subtitle text while:
            1. Keeping translations concise (suitable for subtitles)
            2. Maintaining consistency with previous context
            3. Preserving the natural flow of dialogue
            4. Using colloquial expressions when appropriate
            
            Only respond with the translated text, nothing else.
            """;

        String userPrompt = String.format(
                "Previous context: %s\n\nTranslate from %s to %s:\n%s",
                previousContext != null ? previousContext : "None",
                sourceLang, targetLang, text
        );

        return chatService.chat(systemPrompt, userPrompt).trim();
    }

    /**
     * Batch translate multiple texts
     */
    public List<String> translateBatch(List<String> texts, String sourceLanguage, String targetLanguage) {
        return texts.stream()
                .map(text -> translate(text, sourceLanguage, targetLanguage))
                .toList();
    }

    /**
     * Detect the language of the given text
     */
    public String detectLanguage(String text) {
        String systemPrompt = """
            You are a language detection expert. Identify the language of the given text.
            Respond with only the ISO 639-1 language code (e.g., 'en', 'ar', 'es', 'fr').
            """;

        return chatService.chat(systemPrompt, text).trim().toLowerCase();
    }

    /**
     * Get supported languages
     */
    public Map<String, String> getSupportedLanguages() {
        return LANGUAGE_NAMES;
    }
}
