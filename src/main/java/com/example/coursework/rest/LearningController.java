package com.example.coursework.rest;

import com.example.coursework.annotations.Loggable;
import com.example.coursework.dto.AnswerResultDto;
import com.example.coursework.dto.CardProgressDto;
import com.example.coursework.model.User;
import com.example.coursework.service.LearningService;
import com.example.coursework.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learn")
@Loggable
public class LearningController {

    private final LearningService learningService;
    private final UserService userService;

    @Autowired
    public LearningController(LearningService learningService, UserService userService) {
        this.learningService = learningService;
        this.userService = userService;
    }

    @GetMapping("/get-cards")
    public ResponseEntity<List<CardProgressDto>> getCardsForLearning() {
        User user = userService.getCurrentUser();

        List<CardProgressDto> cards = learningService.getCardsForLearning(user);
        return ResponseEntity.ok(cards);
    }

    @PostMapping("/answer")
    public ResponseEntity<List<AnswerResultDto>> processAnswers(@RequestBody Map<Long, Boolean> answers) {
        User user = userService.getCurrentUser();

        List<AnswerResultDto> results = learningService.processAnswers(user, answers);
        return ResponseEntity.ok(results); // Повертаємо список результатів
    }
}