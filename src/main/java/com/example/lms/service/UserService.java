package com.example.lms.service;

import com.example.lms.model.User;
import com.example.lms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userData) {
        Optional<User> existing = userRepository.findById(id);
        if (existing.isPresent()) {
            User user = existing.get();
            user.setUsername(userData.getUsername());
            user.setEmail(userData.getEmail());
            user.setName(userData.getName());
            user.setPassword(userData.getPassword());
            user.setRole(userData.getRole());
            return userRepository.save(user);
        } else {
            return null;
        }
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Ban a user by setting their role to "BANNED"
    public User banUser(Long userId) {
        // Find the user by ID
        User user = userRepository.findById(userId).orElse(null);

        if (user != null && !user.getRole().equals("ADMIN")) {
            // Update the user's status or role
            user.setRole("BANNED"); // Or another status field like user.setStatus("BANNED");
            return userRepository.save(user); // Save the updated user in the database
        }

        return null; // Return null if user is not found or if it's an admin
    }
}
