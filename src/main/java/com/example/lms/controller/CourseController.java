package com.example.lms.controller;

import com.example.lms.model.Course;
import com.example.lms.model.Teacher;
import com.example.lms.service.CourseService;
import com.example.lms.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private TeacherService teacherService;

    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    @GetMapping("/{id}")
    public Course getCourse(@PathVariable Long id) {
        return courseService.getCourseById(id);
    }

    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody Course course) {
        Optional<Teacher> teacher = teacherService.getTeacherById(course.getTeacher().getId()); // Fetch teacher based on the ID

        if (teacher.isEmpty()) {
            return ResponseEntity.badRequest().body("Teacher not found");
        }

        // Set the teacher for the course
        course.setTeacher(teacher.get());

        return courseService.saveOrUpdateCourse(course); // Save or update the course
    }


    @DeleteMapping("/{id}")
    public void deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
    }
}