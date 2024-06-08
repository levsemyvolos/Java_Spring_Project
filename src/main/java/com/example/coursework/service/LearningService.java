package com.example.coursework.service;

import com.example.coursework.dto.CardProgressDto;
import com.example.coursework.model.Card;
import com.example.coursework.model.CardStatus;
import com.example.coursework.model.User;
import com.example.coursework.model.UserProgress;
import com.example.coursework.model.UserCardId;
import com.example.coursework.repository.CardRepository;
import com.example.coursework.repository.UserProgressRepository;
import com.example.coursework.mapper.CardProgressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LearningService {

    private static final int MAX_WORDS_IN_DECK = 5;
    private static final double EASE_INCREMENT = 0.15;
    private static final double EASE_DECREMENT = 0.2;
    private static final double MIN_EASE = 1.3;
    private static final int MIN_INTERVAL = 1;  // in days
    private static final int MAX_INTERVAL = 365;  // in days
    private final CardRepository cardRepository;
    private final UserProgressRepository userProgressRepository;
    private final CardProgressMapper cardProgressMapper;
    private final TimeFormattingService timeFormattingService;

    @Autowired
    public LearningService(CardRepository cardRepository, UserProgressRepository userProgressRepository,
                           CardProgressMapper cardProgressMapper, TimeFormattingService timeFormattingService) {
        this.cardRepository = cardRepository;
        this.userProgressRepository = userProgressRepository;
        this.cardProgressMapper = cardProgressMapper;
        this.timeFormattingService = timeFormattingService;
    }

    public List<CardProgressDto> getCardsForLearning(User user) {
        List<UserProgress> cardsInDeck = userProgressRepository.findUserProgressWithCardByUserAndStatus(user, CardStatus.IN_DECK);

        if (cardsInDeck.size() == MAX_WORDS_IN_DECK) {
            return cardsInDeck.stream()
                    .sorted(Comparator.comparing(UserProgress::getDue))
                    .map(up -> {
                        CardProgressDto dto = cardProgressMapper.toDto(up.getCard(), user, up);
                        formatTimeFields(dto, up);
                        return dto;
                    })
                    .collect(Collectors.toList());
        } else {
            List<Card> newDeck = getNewCardsForDeck(user, cardsInDeck);
            return newDeck.stream()
                    .map(card -> {
                        Optional<UserProgress> progress = userProgressRepository.findByUserAndCard(user, card);
                        CardProgressDto dto = cardProgressMapper.toDto(card, user, progress.orElse(null));
                        if (progress.isPresent()) {
                            formatTimeFields(dto, progress.get());
                        } else {
                            // Set default due dates for new cards
                            dto.setDueFormattedTrue(timeFormattingService.formatTimeUntil(LocalDateTime.now().plusMinutes(10)));
                            dto.setDueFormattedFalse(timeFormattingService.formatTimeUntil(LocalDateTime.now().plusMinutes(1)));
                        }
                        return dto;
                    })
                    .collect(Collectors.toList());
        }
    }

    @Transactional
    public void processAnswers(User user, Map<Long, Boolean> answers) {
        if (answers.size() < MAX_WORDS_IN_DECK) {
            return;
        }

        answers.forEach((cardId, isCorrect) -> {
            UserProgress progress = userProgressRepository.findByUserAndCardId(user.getId(), cardId)
                    .orElseGet(() -> new UserProgress(user, cardRepository.findById(cardId)
                            .orElseThrow(() -> new RuntimeException("Card not found"))));
            updateProgress(progress, isCorrect);
            userProgressRepository.save(progress);
        });
    }

    private void updateProgress(UserProgress progress, boolean isCorrect) {
        if (isCorrect) {
            progress.setReps(progress.getReps() + 1);
            if (progress.getReps() == 1) {
                progress.setInterval(1);
                progress.setDue(LocalDateTime.now().plusMinutes(10));
            } else if (progress.getReps() == 2) {
                progress.setInterval(6);
                progress.setDue(LocalDateTime.now().plusMinutes(30));
            } else {
                progress.setInterval((int) Math.max(MIN_INTERVAL, Math.min(progress.getInterval() * progress.getEase(), MAX_INTERVAL)));
                progress.setDue(LocalDateTime.now().plus(progress.getInterval(), ChronoUnit.DAYS));
            }
            progress.setEase(Math.max(MIN_EASE, progress.getEase() + EASE_INCREMENT));
        } else {
            progress.setReps(0);
            progress.setInterval(1);
            progress.setEase(Math.max(MIN_EASE, progress.getEase() - EASE_DECREMENT));
            progress.setDue(LocalDateTime.now().plusMinutes(1));
        }
        progress.setDue(LocalDateTime.now().plusMinutes(progress.getInterval()));

        progress.setLearnedLevel(progress.getLearnedLevel() + (isCorrect ? 1 : 0));
        progress.setLastAnswered(LocalDateTime.now());
        progress.setStatus(CardStatus.READY);
    }

    private List<Card> getNewCardsForDeck(User user, List<UserProgress> existingDeck) {
        List<Card> newDeck = new ArrayList<>(MAX_WORDS_IN_DECK);

        List<UserProgress> readyCards = userProgressRepository.findUserProgressWithCardByUserAndStatus(user, CardStatus.READY);
        List<Card> newCards = cardRepository.findNewCardsForUser(user.getId());

        readyCards.sort(Comparator.comparing(UserProgress::getDue));
        Iterator<UserProgress> readyIterator = readyCards.iterator();
        Iterator<Card> newCardIterator = newCards.iterator();

        boolean useReady = true;
        while (newDeck.size() < MAX_WORDS_IN_DECK && (readyIterator.hasNext() || newCardIterator.hasNext())) {
            if (useReady && readyIterator.hasNext()) {
                newDeck.add(readyIterator.next().getCard());
            } else if (newCardIterator.hasNext()) {
                newDeck.add(newCardIterator.next());
            }
            useReady = !useReady;
        }

        newDeck.forEach(card -> {
            UserProgress progress = userProgressRepository.findById(new UserCardId(user.getId(), card.getId()))
                    .orElseGet(() -> new UserProgress(user, card));
            progress.setStatus(CardStatus.IN_DECK);
            userProgressRepository.save(progress);
        });

        return newDeck;
    }

    private void formatTimeFields(CardProgressDto dto, UserProgress progress) {
        dto.setDueFormattedTrue(timeFormattingService.formatTimeUntil(LocalDateTime.now().plusMinutes(calculateDueTime(progress, true))));
        dto.setDueFormattedFalse(timeFormattingService.formatTimeUntil(LocalDateTime.now().plusMinutes(calculateDueTime(progress, false))));
    }

    private int calculateDueTime(UserProgress progress, boolean isCorrect) {
        int interval;
        if (isCorrect) {
            progress.setReps(progress.getReps() + 1);
            if (progress.getReps() == 1) {
                interval = 10; // 10 minutes
            } else if (progress.getReps() == 2) {
                interval = 30; // 30 minutes
            } else {
                interval = (int) Math.max(MIN_INTERVAL, Math.min(progress.getInterval() * progress.getEase(), MAX_INTERVAL));
            }
        } else {
            progress.setReps(0);
            double decreaseFactor = 1 - (progress.getEase() - MIN_EASE) / (2.5 - MIN_EASE); // Чим вищий ease, тим менший decreaseFactor
            interval = (int) Math.round(progress.getInterval() * decreaseFactor);
            interval = Math.max(interval, MIN_INTERVAL);
        }
        return interval; // Повертаємо лише інтервал, без додавання часу
    }

}