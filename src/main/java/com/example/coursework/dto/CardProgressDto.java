package com.example.coursework.dto;

import lombok.Data;

@Data
public class CardProgressDto {
    private Long userId;
    private Long cardId;
    private String word;
    private String sentence;
    private String translation;
    private String type;
    private String synonyms;
    private String lastAnsweredFormatted;
    private String dueFormattedTrue;
    private String dueFormattedFalse;
}