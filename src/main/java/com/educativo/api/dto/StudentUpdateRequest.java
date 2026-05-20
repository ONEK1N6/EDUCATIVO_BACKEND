package com.educativo.api.dto;

import com.educativo.api.entity.EducationLevel;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudentUpdateRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String dni;
    private String section;
    private String grade;
    private EducationLevel level;
}
