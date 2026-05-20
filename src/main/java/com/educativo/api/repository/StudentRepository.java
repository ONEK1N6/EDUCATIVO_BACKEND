package com.educativo.api.repository;

import com.educativo.api.entity.EducationLevel;
import com.educativo.api.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByLevelAndGradeAndSection(EducationLevel level, String grade, String section);
}
