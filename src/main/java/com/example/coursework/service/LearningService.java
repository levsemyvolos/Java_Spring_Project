package com.example.coursework.service;

import com.example.coursework.annotations.Loggable;
import com.example.coursework.dto.AnswerResultDto;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@Loggable
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
    public LearningService(CardRepository cardRepository, UserProgressRepository userProgressRepository, CardProgressMapper cardProgressMapper, TimeFormattingService timeFormattingService) {
        this.cardRepository = cardRepository;
        this.userProgressRepository = userProgressRepository;
        this.cardProgressMapper = cardProgressMapper;
        this.timeFormattingService = timeFormattingService;
    }

    public List<CardProgressDto> getCardsForLearning(User user) {
        List<UserProgress> cardsInDeck = userProgressRepository.findUserProgressWithCardByUserAndStatus(user, CardStatus.IN_DECK);

        if (cardsInDeck.size() == MAX_WORDS_IN_DECK) {
            return cardsInDeck.stream().sorted(Comparator.comparing(UserProgress::getDue)).map(up -> {
                CardProgressDto dto = cardProgressMapper.toDto(up.getCard(), user, up);
                formatTimeFields(dto, up); // Форматуємо час в DTO
                return dto;
            }).collect(Collectors.toList());
        } else {
            List<Card> newDeck = getNewCardsForDeck(user);
            return newDeck.stream().map(card -> {
                Optional<UserProgress> progress = userProgressRepository.findByUserAndCard(user, card);
                CardProgressDto dto = cardProgressMapper.toDto(card, user, progress.orElse(null));
                if (progress.isPresent()) {
                    formatTimeFields(dto, progress.get());  // Форматуємо час в DTO
                } else {
                    // Set default due dates for new cards
                    dto.setDueFormattedTrue(timeFormattingService.formatTimeUntil(LocalDateTime.now().plusMinutes(10)));
                    dto.setDueFormattedFalse(timeFormattingService.formatTimeUntil(LocalDateTime.now().plusMinutes(1)));
                }
                return dto;
            }).collect(Collectors.toList());
        }
    }

    @Transactional
    public List<AnswerResultDto> processAnswers(User user, Map<Long, Boolean> answers) {
        if (answers.size() < MAX_WORDS_IN_DECK) {
            return Collections.emptyList();
        }

        List<UserProgress> cardsInDeck = userProgressRepository.findUserProgressWithCardByUserAndStatus(user, CardStatus.IN_DECK);

        for (Long cardId : answers.keySet()) {
            if (cardsInDeck.stream().noneMatch(up -> up.getCard().getId().equals(cardId))) {
                return Collections.emptyList();
            }
        }

        List<AnswerResultDto> results = new ArrayList<>();

        for (UserProgress userProgress : cardsInDeck) {
            Long cardId = userProgress.getCard().getId();
            boolean isCorrect = answers.get(cardId);

            UserProgress progress = userProgressRepository.findByUserAndCardId(user.getId(), cardId).orElseThrow(() -> new RuntimeException("Card not found"));
            updateProgress(progress, isCorrect);
            userProgressRepository.save(progress);

            AnswerResultDto result = new AnswerResultDto();
            result.setCardId(cardId);
            result.setWord(progress.getCard().getWord());
            result.setTranslation(progress.getCard().getTranslation());
            result.setIsCorrect(isCorrect);
            result.setDueFormatted(timeFormattingService.formatTimeUntil(progress.getDue()));

            results.add(result);
        }

        if (!cardsInDeck.isEmpty()) {
            AnswerResultDto lastResult = results.get(results.size() - 1);
            lastResult.setIsLastCard(true);
        }

        return results;
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
                progress.setDue(LocalDateTime.now().plusDays(progress.getInterval()));
            }
            progress.setEase(Math.max(MIN_EASE, progress.getEase() + EASE_INCREMENT));
        } else {
            progress.setReps(0);
            progress.setInterval(1);
            progress.setEase(Math.max(MIN_EASE, progress.getEase() - EASE_DECREMENT));
            progress.setDue(LocalDateTime.now().plusMinutes(1));
        }
        progress.setLearnedLevel(progress.getLearnedLevel() + (isCorrect ? 1 : 0));
        progress.setLastAnswered(LocalDateTime.now());
        progress.setStatus(CardStatus.READY);
    }

    private List<Card> getNewCardsForDeck(User user) {
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
            UserProgress progress = userProgressRepository.findById(new UserCardId(user.getId(), card.getId())).orElseGet(() -> new UserProgress(user, card));
            progress.setStatus(CardStatus.IN_DECK);
            userProgressRepository.save(progress);
        });

        return newDeck;
    }

    private void formatTimeFields(CardProgressDto dto, UserProgress progress) {
        if (progress.getLastAnswered() != null) {
            dto.setLastAnsweredFormatted(timeFormattingService.formatTimeAgo(progress.getLastAnswered()));
        } else {
            dto.setLastAnsweredFormatted("Нове слово");
        }
        dto.setDueFormattedTrue(timeFormattingService.formatTimeUntil(LocalDateTime.now().plusMinutes(calculateDueTime(progress, true))));
        dto.setDueFormattedFalse(timeFormattingService.formatTimeUntil(LocalDateTime.now().plusMinutes(calculateDueTime(progress, false))));
    }

    private int calculateDueTime(UserProgress progress, boolean isCorrect) {
        int interval;
        if (isCorrect) {
            progress.setReps(progress.getReps() + 1);
            if (progress.getReps() == 1) {
                interval = 10;
            } else if (progress.getReps() == 2) {
                interval = 30;
            } else {
                interval = (int) Math.max(MIN_INTERVAL, Math.min(progress.getInterval() * progress.getEase(), MAX_INTERVAL));
            }
        } else {
            progress.setReps(0);
            interval = 1;
        }
        return interval;
    }

}