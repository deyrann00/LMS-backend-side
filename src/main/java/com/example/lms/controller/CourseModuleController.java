package com.example.lms.controller;

import com.example.lms.model.Course;
import com.example.lms.model.CourseModule;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.CourseModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CourseModuleController {

    @Autowired
    private CourseModuleRepository courseModuleRepository;

    @Autowired
    private CourseRepository courseRepository;

    // ✅ Get modules by course ID
    @GetMapping("/courses/{courseId}/modules")
    public ResponseEntity<List<CourseModule>> getModules(@PathVariable Long courseId) {
        return ResponseEntity.ok(courseModuleRepository.findByCourseId(courseId));
    }

    // ✅ Add new module to course
    @PostMapping("/courses/{courseId}/modules")
    public ResponseEntity<CourseModule> addModule(@PathVariable Long courseId, @RequestBody CourseModule courseModule) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        courseModule.setCourse(course);
        return ResponseEntity.ok(courseModuleRepository.save(courseModule));
    }

    // ✅ Update existing module
    @PutMapping("/modules/{id}")
    public ResponseEntity<CourseModule> updateModule(@PathVariable Long id, @RequestBody CourseModule dto) {
        return courseModuleRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(dto.getTitle());
                    existing.setContent(dto.getContent());
                    return ResponseEntity.ok(courseModuleRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Delete module
    @DeleteMapping("/modules/{id}")
    public ResponseEntity<?> deleteModule(@PathVariable Long id) {
        if (!courseModuleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        courseModuleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/modules/{moduleId}")
    public ResponseEntity<CourseModule> getModuleById(@PathVariable Long moduleId) {
        return courseModuleRepository.findById(moduleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}