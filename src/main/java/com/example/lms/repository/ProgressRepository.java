package com.example.lms.repository;

import com.example.lms.model.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {

    // Find progress by userId and courseId
    Optional<Progress> findByUserIdAndCourseId(Long userId, Long courseId);
}
