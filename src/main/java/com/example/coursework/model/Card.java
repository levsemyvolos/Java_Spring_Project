package com.example.coursework.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "cards")
@Data
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String word;

    @Column(nullable = false)
    private String sentence;

    @Column(nullable = false)
    private String translation;

    @Column(nullable = false)
    private String type;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<UserProgress> progress;

}
