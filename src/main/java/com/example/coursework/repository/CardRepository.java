package com.example.coursework.repository;

import com.example.coursework.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByWordIgnoreCaseContaining(String word);

    List<Card> findByTypeIgnoreCaseContaining(String type);
}