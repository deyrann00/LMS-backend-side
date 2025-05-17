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
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
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
        course.setImageUrl(courseRequest.getImageUrl());  // Set image URL

        if (courseRequest.getTeacherId() != null) {
            Optional<Teacher> optionalTeacher = teacherRepository.findById(courseRequest.getTeacherId());
            if (optionalTeacher.isPresent()) {
                course.setTeacher(optionalTeacher.get());
            } else {
                return ResponseEntity.badRequest().build();
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
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @RequestBody CourseRequest courseRequest) {

        Optional<Course> optionalCourse = courseRepository.findById(id);
        if (optionalCourse.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Course course = optionalCourse.get();

        // Update fields
        course.setTitle(courseRequest.getTitle());
        course.setDescription(courseRequest.getDescription());

        // Update image URL if provided
        if (courseRequest.getImageUrl() != null) {
            course.setImageUrl(courseRequest.getImageUrl());
        }

        // Update teacher if provided
        if (courseRequest.getTeacherId() != null) {
            Optional<Teacher> optionalTeacher = teacherRepository.findById(courseRequest.getTeacherId());
            if (optionalTeacher.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            course.setTeacher(optionalTeacher.get());
        }

        Course savedCourse = courseRepository.save(course);
        return ResponseEntity.ok(savedCourse);
    }

    @PostMapping("/{id}/upload-image")
    public ResponseEntity<Map<String, String>> uploadCourseImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        try {
            Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "course-images");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";

            int i = originalFilename.lastIndexOf('.');
            if (i > 0) {
                extension = originalFilename.substring(i);
            }

            String filename = "course-" + id + extension;
            Path filePath = uploadDir.resolve(filename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("File saved to: " + filePath.toAbsolutePath());

            String imageUrl = "/course-images/" + filename;

            Optional<Course> courseOpt = courseRepository.findById(id);
            if (courseOpt.isPresent()) {
                Course course = courseOpt.get();
                course.setImageUrl(imageUrl);
                courseRepository.save(course);
            }

            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Could not save file"));
        }
    }

    @GetMapping("/{id}/teachers")
    public ResponseEntity<List<Teacher>> getCourseTeachers(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }

        List<Teacher> teachers = teacherService.getTeachersByCourse(course);
        return ResponseEntity.ok(teachers);
    }
}