package com.example.lms.controller;

import com.example.lms.model.User;
import com.example.lms.repository.UserRepository;
import com.example.lms.service.UserService;
import com.example.lms.util.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping({"/api/users", "/api/auth"})
public class UserController {

    @Autowired
    private UserRepository userRepository;

    private final PasswordHasher passwordEncoder = new PasswordHasher();

    @Autowired
    private UserService userService;

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
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        // Encode (hash) the password before saving
        user.setPassword(passwordEncoder.hashPassword(user.getPassword()));

        // Save the user
        User savedUser = userRepository.save(user);

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
}
