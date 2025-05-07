package com.example.lms.dto;

import java.sql.Timestamp; // Ensure this import is correct
import java.util.Map;

public class TestResultDTO {
    private int score;
    private int totalQuestions;
    private Map<Long, Integer> correctAnswers;
    private Map<Long, Integer> studentAnswers;
    private Map<Long, String> options;
    private Timestamp completedAt; // Use java.sql.Timestamp here

    // Getters and setters
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Map<Long, Integer> getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Map<Long, Integer> correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Map<Long, Integer> getStudentAnswers() {
        return studentAnswers;
    }

    public void setStudentAnswers(Map<Long, Integer> studentAnswers) {
        this.studentAnswers = studentAnswers;
    }

    public Map<Long, String> getOptions() {
        return options;
    }

    public void setOptions(Map<Long, String> options) {
        this.options = options;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }
}