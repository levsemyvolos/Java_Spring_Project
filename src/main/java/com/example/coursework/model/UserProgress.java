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

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    private double ease;
    private LocalDateTime due;
    private int interval;
    private int reps;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status = CardStatus.IN_DECK;

    // Default constructor for JPA
    public UserProgress() {
    }

    // Constructor to initialize user and card
    public UserProgress(User user, Card card) {
        this.id = new UserCardId(user.getId(), card.getId());
        this.user = user;
        this.card = card;
        this.learnedLevel = 0;
        this.lastUpdated = LocalDateTime.now();
        this.ease = 2.5; // Default ease factor, adjust as necessary
        this.due = LocalDateTime.now().plusDays(1); // Initial due date
        this.interval = 1; // Initial interval
        this.reps = 0; // Initial repetition count
        this.status = CardStatus.IN_DECK;
    }
}