package com.elsherif.livecaption.ai.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String content;
    private String finishReason;
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    private String model;
}
