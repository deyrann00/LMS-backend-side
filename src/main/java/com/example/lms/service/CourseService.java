package com.example.lms.service;

import com.example.lms.model.Course;
import com.example.lms.model.Student;
import com.example.lms.model.User;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private StudentService studentService;

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id).orElse(null);
    }

    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }

    public Course updateCourse(Long id, Course courseData) {
        Optional<Course> existing = courseRepository.findById(id);
        if (existing.isPresent()) {
            Course course = existing.get();
            course.setTitle(courseData.getTitle());
            course.setDescription(courseData.getDescription());
            return courseRepository.save(course);
        } else {
            return null;
        }
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    public void unsubscribeStudent(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        student.getEnrolledCourses().remove(course);
        studentRepository.save(student);
    }

    public void enrollStudent(Long userId, Long courseId) {
        // Fetch user
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create student if not already exists
        Student student = studentRepository.findByUserId(userId).orElseGet(() -> {
            Student newStudent = new Student();
            newStudent.setUser(user);
            Student savedStudent = studentRepository.save(newStudent);

            // Promote user to STUDENT if not already
            if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
                user.setRole("STUDENT");
                userService.saveUser(user);
            }

            System.out.println("Created student and promoted role for user ID: " + userId);
            return savedStudent;
        });

        // Fetch course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Avoid duplicate enrollment
        if (!student.getEnrolledCourses().contains(course)) {
            student.getEnrolledCourses().add(course);
            studentRepository.save(student);
        } else {
            System.out.println("User already enrolled in course ID: " + courseId);
        }
    }
}
