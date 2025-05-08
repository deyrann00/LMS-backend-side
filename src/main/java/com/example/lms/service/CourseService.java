package com.example.lms.service;

import com.example.lms.model.Course;
import com.example.lms.model.Student;
import com.example.lms.model.Teacher;
import com.example.lms.model.User;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.StudentRepository;
import com.example.lms.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    private TeacherRepository teacherRepository;

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
        // Ensure the teacher is set with the correct teacher_id
        Optional<Teacher> teacherOptional = teacherRepository.findById(course.getTeacher().getId());
        if (teacherOptional.isPresent()) {
            course.setTeacher(teacherOptional.get());
            return courseRepository.save(course);
        } else {
            throw new RuntimeException("Teacher not found");
        }
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

    public void unsubscribeStudent(Long userId, Long courseId) {
        // Step 1: Fetch user
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 2: Fetch student linked to the user
        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        // Step 3: Fetch course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Step 4: Unenroll only if currently enrolled
        if (student.getEnrolledCourses().contains(course)) {
            student.getEnrolledCourses().remove(course);
            studentRepository.save(student);
        } else {
            System.out.println("User is not enrolled in courseId = " + courseId);
        }
    }

    public void enrollStudent(Long userId, Long courseId) {
        // Step 1: Fetch user by ID (primary ID from users table)
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 2: Find student linked to user OR create new one
        Student student = studentRepository.findByUserId(user.getId()).orElse(null);
        if (student == null) {
            student = new Student();
            student.setUser(user);
            student = studentRepository.save(student);

            // Promote user to STUDENT role
            if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
                user.setRole("STUDENT");
                userService.saveUser(user);
            }

            System.out.println("New student created and promoted for userId = " + userId);
        }

        // Step 3: Fetch course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Step 4: Enroll only if not already enrolled
        if (!student.getEnrolledCourses().contains(course)) {
            student.getEnrolledCourses().add(course);
            studentRepository.save(student);
        } else {
            System.out.println("User is already enrolled in courseId = " + courseId);
        }
    }

    public boolean deleteCourse(Long id) {
        Optional<Course> course = courseRepository.findById(id);
        if (course.isPresent()) {
            Course existingCourse = course.get();

            // Очистка записей студентов на этот курс
            if (!existingCourse.getStudents().isEmpty()) {
                for (Student student : existingCourse.getStudents()) {
                    student.getEnrolledCourses().remove(existingCourse);
                }
                existingCourse.getStudents().clear();  // очистка с другой стороны
            }

            // Сохраняем обновлённые связи
            courseRepository.save(existingCourse);

            // Теперь можно безопасно удалить курс
            courseRepository.deleteById(id);
            return true;
        }
        return false; // курс не найден
    }

}
