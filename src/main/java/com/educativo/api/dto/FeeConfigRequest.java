package com.educativo.api.dto;

import lombok.Data;

@Data
public class FeeConfigRequest {
    private Double monthlyAmount;
    private Double registrationAmount;
    private Integer dueDay;
}
