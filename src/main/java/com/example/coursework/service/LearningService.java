package com.example.coursework.service;

import com.example.coursework.model.*;
import com.example.coursework.repository.CardRepository;
import com.example.coursework.repository.UserProgressRepository;
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

    @Autowired
    public LearningService(CardRepository cardRepository, UserProgressRepository userProgressRepository) {
        this.cardRepository = cardRepository;
        this.userProgressRepository = userProgressRepository;
    }

    public List<Card> getCardsForLearning(User user) {
        List<UserProgress> cardsInDeck = userProgressRepository.findUserProgressWithCardByUserAndStatus(user, CardStatus.IN_DECK);

        if (cardsInDeck.size() >= MAX_WORDS_IN_DECK) {
            return cardsInDeck.stream()
                    .sorted(Comparator.comparing(UserProgress::getDue))  // Sort by due date
                    .map(UserProgress::getCard)
                    .collect(Collectors.toList());
        } else {
            return getNewCardsForDeck(user, cardsInDeck);
        }
    }

    @Transactional
    public void processAnswers(User user, Map<Long, Boolean> answers) {
        answers.forEach((cardId, isCorrect) -> {
            UserProgress progress = userProgressRepository.findById(new UserCardId(user.getId(), cardId))
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
            } else if (progress.getReps() == 2) {
                progress.setInterval(6);
            } else {
                progress.setInterval((int) Math.max(MIN_INTERVAL, Math.min(progress.getInterval() * progress.getEase(), MAX_INTERVAL)));
            }
            progress.setEase(Math.max(MIN_EASE, progress.getEase() + EASE_INCREMENT));
        } else {
            progress.setReps(0);
            progress.setInterval(1);
            progress.setEase(Math.max(MIN_EASE, progress.getEase() - EASE_DECREMENT));
        }
        progress.setLearnedLevel(progress.getLearnedLevel() + (isCorrect ? 1 : 0));
        progress.setDue(LocalDateTime.now().plus(progress.getInterval(), ChronoUnit.DAYS));
        progress.setStatus(CardStatus.READY);  // Change the status to READY after updating progress
        progress.setLastUpdated(LocalDateTime.now());
    }

    private List<Card> getNewCardsForDeck(User user, List<UserProgress> existingDeck) {
        List<Card> newDeck = new ArrayList<>(MAX_WORDS_IN_DECK);

        List<UserProgress> readyCards = userProgressRepository.findUserProgressWithCardByUserAndStatus(user, CardStatus.READY);
        List<Card> newCards = cardRepository.findNewCardsForUser(user.getId());

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
            progress.setLastUpdated(LocalDateTime.now());
            userProgressRepository.save(progress);
        });

        return newDeck;
    }
}