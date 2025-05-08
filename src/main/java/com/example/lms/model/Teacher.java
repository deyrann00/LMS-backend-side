package com.example.lms.model;

import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("teacher-course")
    private List<Course> teachingCourses;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Course> getTeachingCourses() {
        return teachingCourses;
    }

    public void setTeachingCourses(List<Course> teachingCourses) {
        this.teachingCourses = teachingCourses;
    }
}