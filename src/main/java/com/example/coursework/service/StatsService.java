package com.example.coursework.service;

import com.example.coursework.annotations.Loggable;
import com.example.coursework.model.CardStatus;
import com.example.coursework.model.User;
import com.example.coursework.model.UserStats;
import com.example.coursework.repository.UserProgressRepository;
import com.example.coursework.repository.UserStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@Loggable
public class StatsService {

    private final UserProgressRepository userProgressRepository;
    private final UserStatsRepository userStatsRepository;

    @Autowired
    public StatsService(UserProgressRepository userProgressRepository, UserStatsRepository userStatsRepository) {
        this.userProgressRepository = userProgressRepository;
        this.userStatsRepository = userStatsRepository;
    }

    @Transactional
    public void updateStats(User user) {
        Optional<UserStats> statsOptional = userStatsRepository.findByUser(user);
        UserStats stats = statsOptional.orElseGet(() -> createUserStats(user));

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        if (!today.isEqual(stats.getLastUpdatedDate())) {
            stats.setWordsLearnedToday(0);
            if (today.getDayOfWeek().getValue() == 1) {
                stats.setWordsLearnedThisWeek(0);
            }
            if (today.getDayOfMonth() == 1) {
                stats.setWordsLearnedThisMonth(0);
            }
        }

        long wordsLearnedToday = userProgressRepository.countByUserAndStatusAndLastAnsweredAfter(
                user, CardStatus.READY, now.truncatedTo(ChronoUnit.DAYS)
        );
        stats.setWordsLearnedToday(wordsLearnedToday);

        long wordsLearnedThisWeek = userProgressRepository.countByUserAndStatusAndLastAnsweredAfter(
                user, CardStatus.READY, now.minusWeeks(1).truncatedTo(ChronoUnit.DAYS)
        );
        stats.setWordsLearnedThisWeek(wordsLearnedThisWeek);

        long wordsLearnedThisMonth = userProgressRepository.countByUserAndStatusAndLastAnsweredAfter(
                user, CardStatus.READY, now.minusMonths(1).truncatedTo(ChronoUnit.DAYS)
        );
        stats.setWordsLearnedThisMonth(wordsLearnedThisMonth);

        stats.setTotalWordsLearned(userProgressRepository.countByUserAndStatus(user, CardStatus.READY));
        stats.setLastUpdatedDate(today);
        userStatsRepository.save(stats);
    }

    private UserStats createUserStats(User user) {
        UserStats stats = new UserStats();
        stats.setUser(user);
        stats.setTotalWordsLearned(0);
        stats.setWordsLearnedToday(0);
        stats.setWordsLearnedThisWeek(0);
        stats.setWordsLearnedThisMonth(0);
        stats.setLastUpdatedDate(LocalDate.now());
        return userStatsRepository.save(stats);
    }

    public UserStats getStatsForUser(User user) {
        return userStatsRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Stats not found for user"));
    }
}