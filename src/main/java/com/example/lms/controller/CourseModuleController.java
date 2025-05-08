package com.example.lms.controller;

import com.example.lms.model.Course;
import com.example.lms.model.CourseModule;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.CourseModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.io.File;
import java.nio.file.Files;

@RestController
@RequestMapping("/api")
public class CourseModuleController {

    @Autowired
    private CourseModuleRepository courseModuleRepository;

    @Autowired
    private CourseRepository courseRepository;

    // File upload path (you can adjust the directory as needed)
    private static final String UPLOAD_DIR = "uploads/";

    // Create directory if it doesn't exist
    static {
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

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
                    existing.setVideoUrl(dto.getVideoUrl()); // Update video URL
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

    // ✅ Get module by ID
    @GetMapping("/modules/{moduleId}")
    public ResponseEntity<CourseModule> getModuleById(@PathVariable Long moduleId) {
        return courseModuleRepository.findById(moduleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint for uploading a file to a module
    @PostMapping("/modules/{moduleId}/upload")
    public ResponseEntity<String> uploadModuleFile(@PathVariable Long moduleId, @RequestParam("file") MultipartFile file) {
        try {
            // Validate the file
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No file uploaded");
            }

            // Get the original filename and save the file to disk
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            Path filePath = Paths.get(UPLOAD_DIR, originalFilename);
            Files.copy(file.getInputStream(), filePath);

            // Save the file path in the module (optional, depending on your use case)
            CourseModule module = courseModuleRepository.findById(moduleId)
                    .orElseThrow(() -> new RuntimeException("Module not found"));
            module.setFilePath(filePath.toString());  // Save the file path
            courseModuleRepository.save(module);  // Save the module with the updated file path

            // Return the file download URI
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/modules/")
                    .path(moduleId.toString())
                    .path("/file/")
                    .path(originalFilename)
                    .toUriString();

            return ResponseEntity.ok("File uploaded successfully. File can be accessed at: " + fileDownloadUri);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed: " + e.getMessage());
        }
    }

    // Endpoint for serving the file for download
    @GetMapping("/modules/{moduleId}/file/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long moduleId, @PathVariable String filename) throws IOException {
        // Fetch the CourseModule from the database to get the file info
        CourseModule module = courseModuleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        // Construct the file path based on uploads folder
        Path path = Paths.get("uploads").resolve(filename);

        // Check if the file exists
        if (!Files.exists(path)) {
            throw new RuntimeException("File not found: " + path);
        }

        // Load the file as a resource
        Resource resource = new UrlResource(path.toUri());

        // Set the content disposition header for downloading
        String contentDisposition = "attachment; filename=\"" + resource.getFilename() + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM) // Content type for file download
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .header(HttpHeaders.CONTENT_ENCODING, "UTF-8")  // Explicitly set encoding header
                .body(resource);
    }
}