package com.educativo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardPaymentRequest {
    @NotBlank
    private String cardNumber;

    @NotBlank
    private String cardHolderName;

    @NotBlank
    private String expirationDate;

    @NotBlank
    private String cvv;

    @NotNull
    private Long feeId;
}
