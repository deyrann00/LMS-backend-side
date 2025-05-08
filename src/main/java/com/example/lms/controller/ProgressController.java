package com.example.lms.controller;

import com.example.lms.model.Progress;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.UserRepository;
import com.example.lms.service.ProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "http://localhost:3000")
public class ProgressController {

    @Autowired
    private ProgressService progressService;

    // Endpoint to get progress for a user and course
    @GetMapping("/{userId}/course/{courseId}")
    public ResponseEntity<Progress> getProgress(@PathVariable Long userId, @PathVariable Long courseId) {
        Progress progress = progressService.getProgress(userId, courseId);
        return progress != null ? ResponseEntity.ok(progress) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // Endpoint to update the progress when a module is completed
    @PostMapping("/{userId}/course/{courseId}")
    public ResponseEntity<Progress> updateModuleCompletion(
            @PathVariable Long userId,
            @PathVariable Long courseId,
            @RequestBody Map<String, Long> requestBody) {
        Long completedModuleId = requestBody.get("completedModuleId");
        progressService.updateProgress(userId, courseId, completedModuleId);
        return ResponseEntity.ok().build();
    }
}