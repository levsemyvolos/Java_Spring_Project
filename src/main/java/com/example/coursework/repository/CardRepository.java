package com.example.coursework.repository;

import com.example.coursework.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByWordIgnoreCaseContaining(String word);

    List<Card> findByTypeIgnoreCaseContaining(String type);

    @Query("SELECT c FROM Card c WHERE c.id NOT IN (SELECT up.card.id FROM UserProgress up WHERE up.user.id = :userId)")
    List<Card> findNewCardsForUser(Long userId);
}
