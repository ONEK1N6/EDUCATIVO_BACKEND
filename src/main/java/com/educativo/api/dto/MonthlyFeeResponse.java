package com.educativo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyFeeResponse {
    private Long id;
    private String month;
    private Integer year;
    private Double amount;
    private String status;
    private String dueDate;
    private String paymentDate;
    private String description;
    private String feeType;
}
