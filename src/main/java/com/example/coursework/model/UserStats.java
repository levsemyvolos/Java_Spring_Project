package com.example.coursework.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "users_stats")
@Data
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private long totalWordsLearned;
    private long wordsLearnedToday;
    private long wordsLearnedThisWeek;
    private long wordsLearnedThisMonth;
    private LocalDate lastUpdatedDate;
}