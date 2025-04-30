package com.example.lms.service;

import com.example.lms.model.Teacher;
import com.example.lms.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    public Teacher saveTeacher(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    public Teacher updateTeacher(Long id, Teacher updated) {
        Optional<Teacher> optional = teacherRepository.findById(id);
        if (optional.isPresent()) {
            Teacher t = optional.get();

            // Update the fields inside the User object
            t.getUser().setName(updated.getUser().getName());
            t.getUser().setEmail(updated.getUser().getEmail());
            t.getUser().setPassword(updated.getUser().getPassword());

            // If you want, you can also update Teacher-specific fields if you had any

            return teacherRepository.save(t);
        }
        return null;
    }

    public void deleteTeacher(Long id) {
        teacherRepository.deleteById(id);
    }
}

