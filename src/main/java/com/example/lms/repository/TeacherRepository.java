package com.example.lms.repository;

import com.example.lms.model.Course;
import com.example.lms.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Teacher findByUserId(Long userId);
    List<Teacher> findByTeachingCourses(Course course);
}
