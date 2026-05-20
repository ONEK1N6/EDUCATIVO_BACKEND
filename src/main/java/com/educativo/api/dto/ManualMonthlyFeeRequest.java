package com.educativo.api.dto;

import com.educativo.api.entity.FeeType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ManualMonthlyFeeRequest {
    @NotNull
    private Long studentId;
    @NotNull
    private Double amount;
    @NotNull
    private LocalDate dueDate;
    private String description;
    @NotNull
    private FeeType feeType;
}
