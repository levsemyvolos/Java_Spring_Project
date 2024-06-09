package com.example.coursework.controller;

import com.example.coursework.model.User;
import com.example.coursework.model.UserStats;
import com.example.coursework.service.StatsService;
import com.example.coursework.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class LearningPageController {

    private final StatsService statsService;
    private final UserService userService;

    @Autowired
    public LearningPageController(StatsService statsService, UserService userService) {
        this.statsService = statsService;
        this.userService = userService;
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
    public String learningPage() {
        return "learn";
    }

}