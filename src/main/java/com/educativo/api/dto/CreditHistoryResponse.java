package com.educativo.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreditHistoryResponse {
    private int totalPaid;
    private int onTimePayments;
    private int latePayments;
    private int pendingPayments;
    private double punctualityPercentage;
    private String creditLevel; // Excelente, Bueno, Regular, Riesgo
    private String color;
}
