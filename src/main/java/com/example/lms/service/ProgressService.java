package com.example.lms.service;

import com.example.lms.model.Progress;
import com.example.lms.model.CourseModule;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.CourseModuleRepository;
import com.example.lms.repository.ProgressRepository;
import com.example.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProgressService {

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseModuleRepository courseModuleRepository;

    @Autowired
    private UserRepository userRepository;

    // This method updates the progress when a module is completed
    public void updateProgress(Long userId, Long courseId, Long completedModuleId) {
        // Fetch the existing progress for the user and course
        Optional<Progress> optionalProgress = progressRepository.findByUserIdAndCourseId(userId, courseId);

        Progress progress = optionalProgress.orElseGet(() -> {
            // If no progress exists, create a new one
            Progress newProgress = new Progress();
            newProgress.setUser(userRepository.findById(userId).get());
            newProgress.setCourse(courseRepository.findById(courseId).get());
            newProgress.setCompletedCourseModules(new ArrayList<>()); // initialize the completed modules list

            // Explicitly fetch the modules from the database (assuming they are lazy-loaded)
            List<CourseModule> courseModules = courseModuleRepository.findByCourseId(courseId);
            newProgress.setTotalModules(courseModules.size());
            newProgress.setPercentageCompleted(0.0);
            newProgress.setLastUpdated(LocalDateTime.now());

            return newProgress;
        });

        // Add the completed module to the progress
        List<Long> completedModules = progress.getCompletedCourseModules();
        if (!completedModules.contains(completedModuleId)) {
            completedModules.add(completedModuleId);
        }
        progress.setCompletedCourseModules(completedModules);

        // Recalculate percentage of completed modules
        double percentageCompleted = (double) completedModules.size() / progress.getTotalModules() * 100;
        progress.setPercentageCompleted(percentageCompleted);

        // Update the last updated time
        progress.setLastUpdated(LocalDateTime.now());

        // Save the progress to the repository
        progressRepository.save(progress);
    }

    // Fetch progress for a given user and course
    public Progress getProgress(Long userId, Long courseId) {
        // Retrieve the progress of a user for a specific course
        Optional<Progress> progressOptional = progressRepository.findByUserIdAndCourseId(userId, courseId);
        return progressOptional.orElseThrow(() -> new RuntimeException("Progress not found for user " + userId + " and course " + courseId));
    }
}