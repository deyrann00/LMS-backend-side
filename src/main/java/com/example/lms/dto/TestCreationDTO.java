package com.example.lms.dto;

import jakarta.validation.Valid;

import java.util.List;

public class TestCreationDTO {
    private String title;

    private Long courseModuleId;

    @Valid
    private List<QuestionCreationDTO> questions;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getCourseModuleId() { return courseModuleId; }
    public void setCourseModuleId(Long courseModuleId) { this.courseModuleId = courseModuleId; }
    public List<QuestionCreationDTO> getQuestions() { return questions; }
    public void setQuestions(List<QuestionCreationDTO> questions) { this.questions = questions; }
}
