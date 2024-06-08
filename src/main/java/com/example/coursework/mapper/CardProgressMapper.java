package com.example.coursework.mapper;

import com.example.coursework.dto.CardProgressDto;
import com.example.coursework.model.Card;
import com.example.coursework.model.User;
import com.example.coursework.model.UserProgress;
import org.springframework.stereotype.Component;

@Component
public class CardProgressMapper {

    public CardProgressDto toDto(Card card, User user, UserProgress progress) {
        CardProgressDto dto = new CardProgressDto();
        dto.setCardId(card.getId());
        dto.setWord(card.getWord());
        dto.setSentence(card.getSentence());
        dto.setTranslation(card.getTranslation());
        dto.setType(card.getType());
        dto.setSynonyms(card.getSynonyms());
        dto.setUserId(user.getId());

        if (progress != null && progress.getLastAnswered() != null) {
            dto.setLastAnsweredFormatted(progress.getLastAnswered().toString());
        } else {
            dto.setLastAnsweredFormatted("Нове слово");
        }

        return dto;
    }
}