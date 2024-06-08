package com.example.coursework.controller;

import com.example.coursework.dto.CardProgressDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/learn")
public class LearningPageController {

    private final RestTemplate restTemplate;

    @Autowired
    public LearningPageController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public String learningPage(Model model) {
        CardProgressDto[] cardsArray = restTemplate.getForObject("http://localhost:8080/api/learn/get-cards", CardProgressDto[].class);
        List<CardProgressDto> cards = Arrays.asList(cardsArray);
        model.addAttribute("cards", cards);
        return "learn";
    }

    @GetMapping("/results")
    public String resultsPage() {
        return "results";
    }
}