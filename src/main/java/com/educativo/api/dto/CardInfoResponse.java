package com.educativo.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CardInfoResponse {
    private String cardNumber;
    private String cardHolderName;
    private String expirationDate;
    private String cvv;
    private Double balance;
}
