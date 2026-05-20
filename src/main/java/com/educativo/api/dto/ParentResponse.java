package com.educativo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParentResponse {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String dni;
    private String email;
    private String phoneNumber;
    private List<String> childrenNames;
}
