package com.example.coursework.repository;

import com.example.coursework.model.Card;
import com.example.coursework.model.CardStatus;
import com.example.coursework.model.User;
import com.example.coursework.model.UserProgress;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    List<UserProgress> findByUserAndStatus(User user, CardStatus status);
    Optional<UserProgress> findByUserAndCard(User user, Card card);
    long countByUserAndStatus(User user, CardStatus cardStatus);

    @Query("SELECT up FROM UserProgress up " +
            "JOIN FETCH up.card " +
            "WHERE up.user = :user AND up.status = :status")
    List<UserProgress> findUserProgressWithCardByUserAndStatus(User user, CardStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE UserProgress up SET up.status = :newStatus WHERE up.user = :user AND up.status = :oldStatus")
    int updateUserProgressStatus(User user, CardStatus oldStatus, CardStatus newStatus);
}
