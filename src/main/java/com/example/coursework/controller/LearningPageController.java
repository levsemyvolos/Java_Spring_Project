package com.example.coursework.controller;

import com.example.coursework.dto.CardProgressDto;
import com.example.coursework.model.User;
import com.example.coursework.model.UserStats;
import com.example.coursework.service.LearningService;
import com.example.coursework.service.StatsService;
import com.example.coursework.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping
public class LearningPageController {

    private final RestTemplate restTemplate;
    private final StatsService statsService;
    private final UserService userService;
    private final LearningService learningService;

    @Autowired
    public LearningPageController(RestTemplate restTemplate, StatsService statsService, UserService userService, LearningService learningService) {
        this.restTemplate = restTemplate;
        this.statsService = statsService;
        this.userService = userService;
        this.learningService = learningService;
    }

    @GetMapping("/stats")
    public String statsPage(Model model) {
        User user = userService.getCurrentUser();

        statsService.updateStats(user);
        UserStats stats = statsService.getStatsForUser(user);
        model.addAttribute("stats", stats);
        return "stats";
    }
    @GetMapping("/learn")
    public String learningPage(Model model) {
        return "learn";
    }

}