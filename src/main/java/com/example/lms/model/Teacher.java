package com.example.lms.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")  // foreign key to User table
    private User user;

    @OneToMany(mappedBy = "teacher")
    private List<Course> courses;
}