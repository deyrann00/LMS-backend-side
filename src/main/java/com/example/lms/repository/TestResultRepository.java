package com.example.lms.repository;

import com.example.lms.model.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    boolean existsByUserIdAndTestId(Long userId, Long testId);
    Optional<TestResult> findByUserIdAndTestId(Long userId, Long testId);
    List<TestResult> findByTestId(Long testId);
}