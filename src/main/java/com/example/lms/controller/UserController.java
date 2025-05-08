package com.example.lms.controller;

import com.example.lms.dto.RegisterRequest;
import com.example.lms.model.Teacher;
import com.example.lms.model.User;
import com.example.lms.repository.UserRepository;
import com.example.lms.service.UserService;
import com.example.lms.util.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RequestMapping({"/api/users", "/api/auth"})
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.lms.repository.TeacherRepository teacherRepository;

    private final PasswordHasher passwordEncoder = new PasswordHasher();

    @Autowired
    private UserService userService;

    private static final String AVATAR_UPLOAD_DIR = "uploads/avatars";

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public Optional<User> getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        // 1. Check for duplicates
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        // 2. Create and save User
        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.hashPassword(request.getPassword()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        // 3. If role is TEACHER, also create Teacher entry
        if ("TEACHER".equalsIgnoreCase(request.getRole())) {
            Teacher teacher = new Teacher();
            teacher.setUser(savedUser);
            teacherRepository.save(teacher);
        }

        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String login = loginData.get("login");
        String password = loginData.get("password");

        Optional<User> userOpt = userRepository.findByEmailOrUsername(login, login);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("User not found");
        }

        User user = userOpt.get();
        String hashedInputPassword = PasswordHasher.hashPassword(password);
        String storedHashedPassword = user.getPassword();

        if (!storedHashedPassword.equals(hashedInputPassword)) {
            return ResponseEntity.status(401).body("Invalid password");
        }

        return ResponseEntity.ok(user);
    }

    // New endpoint to fetch current user
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("User-Id") Long userId) {
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }
        return ResponseEntity.ok(userOpt.get());
    }

    // New endpoint to update user name
    @PutMapping("/update")
    public ResponseEntity<?> updateUserName(@RequestHeader("User-Id") Long userId, @RequestBody Map<String, String> updateData) {
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = userOpt.get();
        String newName = updateData.get("name");
        if (newName == null || newName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Name cannot be empty");
        }

        user.setName(newName);
        userService.saveUser(user);
        return ResponseEntity.ok(user);
    }

    // New endpoint to change password
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("User-Id") Long userId, @RequestBody Map<String, String> passwordData) {
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");
        String confirmPassword = passwordData.get("confirmPassword");

        if (currentPassword == null || newPassword == null || confirmPassword == null) {
            return ResponseEntity.badRequest().body("All password fields are required");
        }

        User user = userOpt.get();
        String hashedInputPassword = passwordEncoder.hashPassword(currentPassword);
        if (!user.getPassword().equals(hashedInputPassword)) {
            return ResponseEntity.status(401).body("Current password is incorrect");
        }

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body("New password and confirmation do not match");
        }

        if (newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("New password cannot be empty");
        }

        user.setPassword(passwordEncoder.hashPassword(newPassword));
        userService.saveUser(user);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PutMapping("/update-avatar")
    public ResponseEntity<?> updateAvatar(
            @RequestHeader("User-Id") Long userId,
            @RequestParam("avatar") MultipartFile avatarFile) {

        System.out.println("Received User-Id: " + userId); // Log the User-Id to check if it is correctly passed

        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userOpt.get();

        try {
            // Check if file is empty
            if (avatarFile.isEmpty()) {
                return ResponseEntity.badRequest().body("No file uploaded");
            }

            // Validate file type (make sure it's an image)
            if (!avatarFile.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().body("Only image files are allowed");
            }

            // Set up the path for avatar file storage
            String avatarFileName = "avatar_" + userId + ".jpg";  // Use the user ID to create a unique file name
            Path avatarPath = Paths.get(AVATAR_UPLOAD_DIR, avatarFileName);

            // Ensure the directory exists
            File avatarDirectory = avatarPath.getParent().toFile();
            if (!avatarDirectory.exists()) {
                avatarDirectory.mkdirs();  // Create the directory if it doesn't exist
            }

            // Copy the file to the server
            Files.copy(avatarFile.getInputStream(), avatarPath, StandardCopyOption.REPLACE_EXISTING);

            // Save the avatar URL in the user's profile
            // The path must be publicly accessible to frontend
            user.setAvatarUrl("/uploads/avatars/" + avatarFileName);  // Ensure this path works for frontend
            userService.saveUser(user);

            return ResponseEntity.ok("Avatar updated successfully");
        } catch (IOException e) {
            // Log the error for debugging
            System.err.println("Error updating avatar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update avatar: " + e.getMessage());
        }
    }

    // Ban a user by ID
    @PutMapping("/{userId}/ban")
    public void banUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setBanned(true);  // Mark the user as banned
        userRepository.save(user);  // Save the updated user
    }
}
