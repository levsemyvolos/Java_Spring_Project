package com.example.coursework.rest;

import com.example.coursework.model.Card;
import com.example.coursework.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    @Autowired
    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Card>> getCardById(@PathVariable Long id) {
        Optional<Card> card = cardService.getCardById(id);
        if (card.isPresent()) {
            return ResponseEntity.ok(card);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/word/{word}")
    public ResponseEntity<List<Card>> getCardsByWord(@PathVariable String word) {
        List<Card> cards = cardService.getCardsByWord(word);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Card>> getCardsByType(@PathVariable String type) {
        List<Card> cards = cardService.getCardsByType(type);
        return ResponseEntity.ok(cards);
    }

    @PostMapping
    public ResponseEntity<Card> createCard(@RequestBody Card card) {
        Card createdCard = cardService.createCard(card);
        return ResponseEntity.created(URI.create("/api/cards/" + createdCard.getId())).body(createdCard);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Card> updateCard(@PathVariable Long id, @RequestBody Card card) {
        card.setId(id); // Встановлюємо ідентифікатор для оновлення
        Card updatedCard = cardService.updateCard(card);
        return ResponseEntity.ok(updatedCard);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}