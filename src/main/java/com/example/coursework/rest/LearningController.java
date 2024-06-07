package com.example.coursework.rest;

import com.example.coursework.model.Card;
import com.example.coursework.model.User;
import com.example.coursework.repository.UserRepository;
import com.example.coursework.service.LearningService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learn")
public class LearningController {

    private final LearningService learningService;
    private final UserRepository userRepository;

    public LearningController(LearningService learningService, UserRepository userRepository) {
        this.learningService = learningService;
        this.userRepository = userRepository;
    }

    @GetMapping("/get-cards")
    public ResponseEntity<List<Card>> getCardsForLearning() {
        // Отримуємо фіксованого користувача з бази даних (замініть на реальну логіку пізніше)
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Card> cards = learningService.getCardsForLearning(user);
        return ResponseEntity.ok(cards);
    }

    @PostMapping("/answer")
    public ResponseEntity<Void> processAnswers(@RequestBody Map<Long, Boolean> answers) {
        // Отримуємо фіксованого користувача з бази даних
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("User not found"));

        learningService.processAnswers(user, answers);
        return ResponseEntity.ok().build();
    }
}