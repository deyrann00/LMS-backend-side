package com.example.lms.service;

import com.example.lms.model.Student;
import com.example.lms.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }

    public Student updateStudent(Long id, Student updated) {
        Optional<Student> optional = studentRepository.findById(id);
        if (optional.isPresent()) {
            Student s = optional.get();
            s.setUser(updated.getUser());
            s.setEnrolledCourses(updated.getEnrolledCourses());
            return studentRepository.save(s);
        }
        return null;
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
}
