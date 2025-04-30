package com.example.lms.controller; // Adjust to your actual package

import com.example.lms.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    @Autowired
    private CourseService courseService;

    @RequestMapping("/enroll")
    public ResponseEntity<?> enrollStudent(@RequestBody Map<String, Long> payload) {
        Long studentId = payload.get("studentId");
        Long courseId = payload.get("courseId");

        if (studentId == null || courseId == null) {
            return ResponseEntity.badRequest().body("Missing studentId or courseId");
        }

        courseService.enrollStudent(studentId, courseId);
        return ResponseEntity.ok().body("Enrollment successful");
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribeStudent(@RequestBody Map<String, Long> payload) {
        Long studentId = payload.get("studentId");
        Long courseId = payload.get("courseId");

        if (studentId == null || courseId == null) {
            return ResponseEntity.badRequest().body("Missing studentId or courseId");
        }

        courseService.unsubscribeStudent(studentId, courseId);
        return ResponseEntity.ok().body("Unsubscribed successfully");
    }
}