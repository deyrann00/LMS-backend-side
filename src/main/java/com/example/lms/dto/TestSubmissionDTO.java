package com.example.lms.dto;

import java.util.Map;

public class TestSubmissionDTO {
    private Map<Long, Integer> answers;

    public Map<Long, Integer> getAnswers() { return answers; }
    public void setAnswers(Map<Long, Integer> answers) { this.answers = answers; }
}