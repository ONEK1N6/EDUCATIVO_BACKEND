package com.educativo.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ParentUpdateRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String dni;
    private String email;
    private String phoneNumber;
}
