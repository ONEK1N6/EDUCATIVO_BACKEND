package com.educativo.api.service;

import com.educativo.api.dto.*;
import com.educativo.api.entity.Student;

import java.util.List;

public interface PaymentService {
    void generateFeesForStudent(Student student);
    List<MonthlyFeeResponse> getFeesByStudent(Long studentId);
    PaymentSummaryResponse getPaymentSummary();
    MessageResponse recordPayment(Long feeId);
    MessageResponse createManualMonthlyFee(ManualMonthlyFeeRequest request);
    MessageResponse createGroupPayment(GroupPaymentRequest request);
    byte[] generatePaymentHistoryPdf(Long studentId);
}
