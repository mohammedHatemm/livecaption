package com.elsherif.livecaption.ai.translation;

import com.elsherif.livecaption.ai.common.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranslationServiceTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private TranslationService translationService;

    @Test
    void translate_shouldTranslateText() {
        when(chatService.chat(anyString(), anyString())).thenReturn("مرحبا بالعالم");

        String result = translationService.translate("Hello World", "en", "ar");

        assertThat(result).isEqualTo("مرحبا بالعالم");
        verify(chatService).chat(anyString(), contains("Hello World"));
    }

    @Test
    void translateSubtitle_shouldIncludeContext() {
        when(chatService.chat(anyString(), anyString())).thenReturn("مرحبا");

        String result = translationService.translateSubtitle("Hello", "en", "ar", "Previous dialogue");

        assertThat(result).isEqualTo("مرحبا");
        verify(chatService).chat(anyString(), contains("Previous context"));
    }

    @Test
    void translateBatch_shouldTranslateMultipleTexts() {
        when(chatService.chat(anyString(), contains("Hello"))).thenReturn("مرحبا");
        when(chatService.chat(anyString(), contains("Goodbye"))).thenReturn("مع السلامة");

        List<String> results = translationService.translateBatch(List.of("Hello", "Goodbye"), "en", "ar");

        assertThat(results).hasSize(2);
        assertThat(results.get(0)).isEqualTo("مرحبا");
        assertThat(results.get(1)).isEqualTo("مع السلامة");
    }

    @Test
    void detectLanguage_shouldReturnLanguageCode() {
        when(chatService.chat(anyString(), anyString())).thenReturn("en");

        String result = translationService.detectLanguage("Hello World");

        assertThat(result).isEqualTo("en");
    }

    @Test
    void getSupportedLanguages_shouldReturnLanguageMap() {
        Map<String, String> languages = translationService.getSupportedLanguages();

        assertThat(languages).containsKey("en");
        assertThat(languages).containsKey("ar");
        assertThat(languages.get("en")).isEqualTo("English");
        assertThat(languages.get("ar")).isEqualTo("Arabic");
    }
}
