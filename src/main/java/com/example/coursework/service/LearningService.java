package com.example.coursework.service;

import com.example.coursework.model.Card;
import com.example.coursework.model.CardStatus;
import com.example.coursework.model.User;
import com.example.coursework.model.UserProgress;
import com.example.coursework.repository.CardRepository;
import com.example.coursework.repository.UserProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LearningService {

    private static final int MAX_WORDS_IN_DECK = 5;
    private final CardRepository cardRepository;
    private final UserProgressRepository userProgressRepository;

    @Autowired
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
            return getNewCardsForDeck(user, cardsInDeck);
        }
    }

    @Transactional
    public void processAnswers(User user, Map<Long, Boolean> answers) {
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
            progress.setLearnedLevel(progress.getLearnedLevel() + 1);
            progress.setReps(progress.getReps() + 1);
            progress.setInterval(calculateInterval(progress));
            progress.setDue(LocalDateTime.now().plusDays(progress.getInterval()));
        } else {
            progress.setLearnedLevel(0);
            progress.setReps(0);
            progress.setInterval(1);
            progress.setDue(LocalDateTime.now().plusDays(1));
        }
        progress.setStatus(CardStatus.READY);
    }

    private int calculateInterval(UserProgress progress) {
        int interval = progress.getInterval();
        double easeFactor = 2.5; // Example value, adjust as necessary
        return (int) (interval * easeFactor);
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
            UserProgress progress = new UserProgress(user, card);
            progress.setStatus(CardStatus.IN_DECK);
            userProgressRepository.save(progress);
        });

        return newDeck;
    }
}