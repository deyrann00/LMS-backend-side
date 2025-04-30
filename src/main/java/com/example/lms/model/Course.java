package com.example.lms.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @ManyToOne
    @JoinColumn(name = "teacher_id") // foreign key to Teacher
    private Teacher teacher;

    @ManyToMany(mappedBy = "enrolledCourses")
    private Set<Student> students = new HashSet<>();

    // getters and setters
}