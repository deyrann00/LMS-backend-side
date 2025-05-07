package com.example.lms.model;

import jakarta.persistence.*;
import java.sql.Timestamp; // Ensure this import is correct

@Entity
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "test_id")
    private Test test;

    private int score;
    private int totalQuestions;

    private Timestamp completedAt; // Use java.sql.Timestamp here

    @Column(columnDefinition = "TEXT")
    private String answers; // JSON с ответами студента

    @Column(unique = true)
    private String userTestKey; // Уникальный ключ (user_id + test_id)

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Test getTest() { return test; }
    public void setTest(Test test) { this.test = test; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }
    public String getAnswers() { return answers; }
    public void setAnswers(String answers) { this.answers = answers; }
    public String getUserTestKey() { return userTestKey; }
    public void setUserTestKey(String userTestKey) { this.userTestKey = userTestKey; }
}