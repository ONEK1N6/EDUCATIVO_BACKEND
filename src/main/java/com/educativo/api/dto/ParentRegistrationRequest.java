package com.educativo.api.dto;

import com.educativo.api.entity.EducationLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ParentRegistrationRequest {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String dni;

    private String email;

    private String phoneNumber;

    @NotEmpty
    private List<StudentRequest> children;

    @Data
    public static class StudentRequest {
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
}
