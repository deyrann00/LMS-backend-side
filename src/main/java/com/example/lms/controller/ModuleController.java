package com.example.lms.controller;

import com.example.lms.model.Course;
import com.example.lms.model.Module;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ModuleController {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private CourseRepository courseRepository;

    // ✅ Get modules by course ID
    @GetMapping("/courses/{courseId}/modules")
    public ResponseEntity<List<Module>> getModules(@PathVariable Long courseId) {
        return ResponseEntity.ok(moduleRepository.findByCourseId(courseId));
    }

    // ✅ Add new module to course
    @PostMapping("/courses/{courseId}/modules")
    public ResponseEntity<Module> addModule(@PathVariable Long courseId, @RequestBody Module module) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        module.setCourse(course);
        return ResponseEntity.ok(moduleRepository.save(module));
    }

    // ✅ Update existing module
    @PutMapping("/modules/{id}")
    public ResponseEntity<Module> updateModule(@PathVariable Long id, @RequestBody Module dto) {
        return moduleRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(dto.getTitle());
                    existing.setContent(dto.getContent());
                    return ResponseEntity.ok(moduleRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Delete module
    @DeleteMapping("/modules/{id}")
    public ResponseEntity<?> deleteModule(@PathVariable Long id) {
        if (!moduleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        moduleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/modules/{moduleId}")
    public ResponseEntity<Module> getModuleById(@PathVariable Long moduleId) {
        return moduleRepository.findById(moduleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}