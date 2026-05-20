package com.educativo.api.dto;

import com.educativo.api.entity.EducationLevel;
import com.educativo.api.entity.FeeType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GroupPaymentRequest {
    @NotNull
    private EducationLevel level;
    @NotNull
    private String grade;
    @NotNull
    private String section;
    
    private Double amount;
    private LocalDate dueDate;
    private String description;
    private FeeType feeType; // If null, maybe it means BOTH? Or I add a boolean.
    
    private boolean generateBoth;
}
