package com.educativo.api.dto;

import com.educativo.api.entity.EducationLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String dni;
    private String section;
    private String grade;
    private EducationLevel level;
    private String parentName;
}
