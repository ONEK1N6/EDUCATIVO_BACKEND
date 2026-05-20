package com.educativo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSummaryResponse {
    private Double totalCollected;
    private Double totalPending;
    private Long overduePaymentsCount;
}
