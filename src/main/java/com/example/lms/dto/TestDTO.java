package com.example.lms.dto;

import com.example.lms.model.CourseModule;

import java.util.List;

public class TestDTO {
    private Long id;
    private String title;
    private CourseModule courseModule;  // Module from com.example.lms.model.Module
    private List<QuestionDTO> questions;  // List of questions (DTO version)

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CourseModule getModule() {
        return courseModule;
    }

    public void setModule(CourseModule courseModule) {
        this.courseModule = courseModule;
    }

    public List<QuestionDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDTO> questions) {
        this.questions = questions;
    }
}