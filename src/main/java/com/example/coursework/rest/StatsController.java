package com.example.coursework.rest;

import com.example.coursework.annotations.Loggable;
import com.example.coursework.model.User;
import com.example.coursework.model.UserStats;
import com.example.coursework.service.StatsService;
import com.example.coursework.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@Loggable
public class StatsController {

    private final StatsService statsService;
    private final UserService userService;

    @Autowired
    public StatsController(StatsService statsService, UserService userService) {
        this.statsService = statsService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserStats> getStats() {
        User user = userService.getCurrentUser();
        statsService.updateStats(user);
        UserStats stats = statsService.getStatsForUser(user);
        return ResponseEntity.ok(stats);
    }
}