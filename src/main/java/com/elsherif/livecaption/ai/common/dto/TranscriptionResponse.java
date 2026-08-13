package com.elsherif.livecaption.ai.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionResponse {
    private String text;
    private String language;
    private Double duration;
    private List<Segment> segments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Segment {
        private int id;
        private double start;
        private double end;
        private String text;
        private List<Word> words;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Word {
        private String word;
        private double start;
        private double end;
    }
}
