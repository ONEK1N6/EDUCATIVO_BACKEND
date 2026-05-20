package com.educativo.api.repository;

import com.educativo.api.entity.MonthlyFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyFeeRepository extends JpaRepository<MonthlyFee, Long> {
    List<MonthlyFee> findByStudentIdOrderByYearAscDueDateAsc(Long studentId);
}
