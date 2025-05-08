package com.example.lms.controller;

import com.example.lms.model.Course;
import com.example.lms.model.Teacher;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.TeacherRepository;
import com.example.lms.service.CourseService;
import com.example.lms.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.lms.dto.CourseRequest;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping
    public List<Course> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        // This is just for checking if teacher is included
        for (Course course : courses) {
            System.out.println(course.getTeacher()); // Debugging log to check if teacher is being fetched
        }
        return courses;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourse(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(course);
    }

    @PostMapping()
    public ResponseEntity<Course> saveCourse(@RequestBody CourseRequest courseRequest) {
        Course course = new Course();
        course.setTitle(courseRequest.getTitle());
        course.setDescription(courseRequest.getDescription());

        if (courseRequest.getTeacherId() != null) {
            Optional<Teacher> optionalTeacher = teacherRepository.findById(courseRequest.getTeacherId());
            if (optionalTeacher.isPresent()) {
                course.setTeacher(optionalTeacher.get());
            } else {
                return ResponseEntity.badRequest().build(); // Or custom error response
            }
        }

        Course savedCourse = courseRepository.save(course);
        return ResponseEntity.ok(savedCourse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCourse(@PathVariable Long id) {
        boolean isDeleted = courseService.deleteCourse(id);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(400).body("Unable to delete course, it may have enrolled students or other dependencies.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Course updatedCourse) {
        Course updated = courseService.updateCourse(id, updatedCourse);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/teachers")
    public ResponseEntity<List<Teacher>> getCourseTeachers(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }

        List<Teacher> teachers = teacherService.getTeachersByCourse(course);  // Assuming you have this method in your TeacherService
        return ResponseEntity.ok(teachers);
    }
}