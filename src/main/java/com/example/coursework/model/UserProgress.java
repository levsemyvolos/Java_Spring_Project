package com.example.coursework.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users_progress")
@Data
public class UserProgress {

    @EmbeddedId
    private UserCardId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("cardId")
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(name = "learned_level")
    private int learnedLevel;

    @Column(name = "last_answered")
    private LocalDateTime lastAnswered;

    private double ease;
    private LocalDateTime due;
    private int interval;
    private int reps;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    // Default constructor for JPA
    public UserProgress() {
    }

    // Constructor to initialize user and card
    public UserProgress(User user, Card card) {
        this.id = new UserCardId(user.getId(), card.getId());
        this.user = user;
        this.card = card;
        this.learnedLevel = 0;
        this.ease = 2.5;
        this.due = LocalDateTime.now().plusMinutes(10);
        this.interval = 1;
        this.reps = 0;
        this.status = CardStatus.IN_DECK;
    }

    @Override
    public String toString() {
        return "UserProgress{" +
                "id=" + id +
                ", user_id='" + user.getId() + '\'' +
                ", card_id='" + card.getId() + '\'' +
                ", learnedLevel='" + learnedLevel + '\'' +
                ", lastAnswered='" + lastAnswered + '\'' +
                ", ease='" + ease + '\'' +
                ", due='" + due + '\'' +
                ", interval='" + interval + '\'' +
                ", reps=" + reps + '\'' +
                ", status=" + status +
                '}'; // Виключено поле progress
    }
}