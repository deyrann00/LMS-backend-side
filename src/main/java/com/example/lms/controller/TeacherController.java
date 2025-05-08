package com.example.lms.controller;

import com.example.lms.dto.TeacherRequest;
import com.example.lms.model.Course;
import com.example.lms.model.Teacher;
import com.example.lms.model.User;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.TeacherRepository;
import com.example.lms.repository.UserRepository;
import com.example.lms.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<Teacher> getAllTeachers() {
        return teacherService.getAllTeachers();
    }

    @PostMapping
    public Teacher createTeacher(@RequestBody Teacher teacher) {
        return teacherService.saveTeacher(teacher);
    }

    @PutMapping("/{id}")
    public Teacher updateTeacher(@PathVariable Long id, @RequestBody Teacher teacher) {
        return teacherService.updateTeacher(id, teacher);
    }

    @DeleteMapping("/{id}")
    public void deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
    }

    @GetMapping("/{userId}/courses")
    public ResponseEntity<List<Course>> getCoursesByTeacher(@PathVariable Long userId) {
        Teacher teacher = teacherRepository.findByUserId(userId);
        if (teacher == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        List<Course> courses = courseRepository.findByTeacherId(teacher.getId());
        return ResponseEntity.ok(courses);
    }
    @GetMapping("/{userId}/courses/{courseId}")
    public ResponseEntity<?> getCourseForTeacher(
            @PathVariable Long userId,
            @PathVariable Long courseId) {

        Teacher teacher = teacherRepository.findByUserId(userId);
        if (teacher == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Teacher not found for user ID: " + userId);
        }

        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Course not found for ID: " + courseId);
        }

        if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("This course does not belong to the teacher");
        }

        return ResponseEntity.ok(course);
    }
}
