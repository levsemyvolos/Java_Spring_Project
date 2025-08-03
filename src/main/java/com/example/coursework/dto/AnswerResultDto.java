package com.example.coursework.dto;

import lombok.Data;

@Data
public class AnswerResultDto {
    private Long cardId;
    private String word;
    private String translation;
    private boolean isCorrect;
    private String dueFormatted;
    private boolean isLastCard;

    public void setIsCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public void setIsLastCard(boolean b) {
        this.isLastCard = b;
    }
}