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
}
