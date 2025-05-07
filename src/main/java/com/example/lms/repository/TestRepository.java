package com.example.lms.repository;

import com.example.lms.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TestRepository extends JpaRepository<Test, Long> {
    @Query("SELECT t FROM Test t WHERE t.courseModule.id = :courseModuleId")
    boolean existsByModuleId(@Param("courseModuleId") Long courseModuleId);
}
