package com.example.lms.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Course course;

    @ElementCollection
    private List<Long> completedCourseModules; // Stores IDs of completed modules

    private int totalModules; // Total number of modules in the course

    private double percentageCompleted; // Percentage of completion (0 to 100%)

    private LocalDateTime lastUpdated; // Last time progress was updated

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public List<Long> getCompletedCourseModules() {
        return completedCourseModules;
    }

    public void setCompletedCourseModules(List<Long> completedCourseModules) {
        this.completedCourseModules = completedCourseModules;
    }

    public int getTotalModules() {
        return totalModules;
    }

    public void setTotalModules(int totalModules) {
        this.totalModules = totalModules;
    }

    public double getPercentageCompleted() {
        return percentageCompleted;
    }

    public void setPercentageCompleted(double percentageCompleted) {
        this.percentageCompleted = percentageCompleted;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}