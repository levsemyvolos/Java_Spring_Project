package com.example.coursework.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserCardId implements Serializable {

    private Long userId;
    private Long cardId;

    public UserCardId() {}

    public UserCardId(Long userId, Long cardId) {
        this.userId = userId;
        this.cardId = cardId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCardId that = (UserCardId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(cardId, that.cardId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, cardId);
    }
}

