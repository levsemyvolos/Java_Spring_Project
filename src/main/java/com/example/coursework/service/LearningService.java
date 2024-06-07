package com.example.coursework.service;

import com.example.coursework.model.Card;
import com.example.coursework.model.CardStatus;
import com.example.coursework.model.User;
import com.example.coursework.model.UserProgress;
import com.example.coursework.repository.CardRepository;
import com.example.coursework.repository.UserProgressRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LearningService {

    private static final int MAX_WORDS_IN_DECK = 5;
    private static final double EASE_FACTOR = 1.5;
    private final CardRepository cardRepository;
    private final UserProgressRepository userProgressRepository;

    public LearningService(CardRepository cardRepository, UserProgressRepository userProgressRepository) {
        this.cardRepository = cardRepository;
        this.userProgressRepository = userProgressRepository;
    }

    public List<Card> getCardsForLearning(User user) {
        List<UserProgress> cardsInDeck = userProgressRepository.findUserProgressWithCardByUserAndStatus(user, CardStatus.IN_DECK);

        if (cardsInDeck.size() == MAX_WORDS_IN_DECK) {
            return cardsInDeck.stream()
                    .map(UserProgress::getCard)
                    .collect(Collectors.toList());
        } else {
            return getNewCardsForDeck(user);
        }
    }

    private List<Card> getNewCardsForDeck(User user) {
        List<UserProgress> readyCards = userProgressRepository.findUserProgressWithCardByUserAndStatus(user, CardStatus.READY);
        List<UserProgress> cardsForDeck = userProgressRepository.findUserProgressWithCardByUserAndStatus(user, CardStatus.IN_DECK);

        int cardsToAdd = MAX_WORDS_IN_DECK - cardsForDeck.size();
        Random random = new Random();

        for (UserProgress progress : readyCards) {
            if (cardsToAdd == 0) {
                break;
            }
            progress.setStatus(CardStatus.IN_DECK);
            cardsForDeck.add(progress);
            cardsToAdd--;
        }

        List<Card> allCards = cardRepository.findAll();
        List<Card> newCards = new ArrayList<>(allCards);
        newCards.removeAll(cardsForDeck.stream().map(UserProgress::getCard).toList());

        for (Card card : newCards) {
            if (cardsToAdd == 0) {
                break;
            }
            UserProgress progress = createProgress(user, card);
            cardsForDeck.add(progress);
            cardsToAdd--;
        }

        userProgressRepository.saveAll(cardsForDeck);
        return cardsForDeck.stream()
                .map(UserProgress::getCard)
                .collect(Collectors.toList());
    }

    public void processAnswers(User user, Map<Long, Boolean> answers) {
        for (Map.Entry<Long, Boolean> answer : answers.entrySet()) {
            Long cardId = answer.getKey();
            Boolean isCorrect = answer.getValue();

            UserProgress progress = userProgressRepository.findByUserAndCard(user, cardRepository.findById(cardId).orElseThrow())
                    .orElseGet(() -> createProgress(user, cardRepository.findById(cardId).orElseThrow()));

            updateProgress(progress, isCorrect);
        }

        // Оновлюємо статус карток після обробки відповідей
        userProgressRepository.updateUserProgressStatus(user, CardStatus.IN_DECK, CardStatus.READY);
    }

    private UserProgress createProgress(User user, Card card) {
        UserProgress progress = new UserProgress();
        progress.setUser(user);
        progress.setCard(card);
        progress.setLearnedLevel(0);
        progress.setLastUpdated(LocalDateTime.now());
        progress.setEase(2.5);
        progress.setInterval(0);
        progress.setReps(0);
        progress.setDue(LocalDateTime.now());
        progress.setStatus(CardStatus.READY);
        return userProgressRepository.save(progress);
    }


    private void updateProgress(UserProgress progress, boolean isCorrect) {
        LocalDateTime now = LocalDateTime.now();
        if (isCorrect) {
            progress.setReps(progress.getReps() + 1);
            progress.setEase(Math.max(1.3, progress.getEase() + 0.1 - (5 - progress.getLearnedLevel()) * (0.08 + (5 - progress.getLearnedLevel()) * 0.02)));
        } else {
            progress.setReps(0);
            progress.setEase(progress.getEase() - 0.2);
        }

        if (progress.getReps() <= 1) {
            progress.setInterval(1);
        } else if (progress.getReps() == 2) {
            progress.setInterval(6);
        } else {
            progress.setInterval((int) Math.round(progress.getEase() * progress.getInterval()));
        }

        progress.setDue(now.plusMinutes(progress.getInterval()));
        progress.setLearnedLevel(progress.getLearnedLevel() + 1);
        progress.setLastUpdated(now);
        userProgressRepository.save(progress);
    }

}