package com.educativo.api.service;

import com.educativo.api.dto.CardInfoResponse;
import com.educativo.api.dto.CardPaymentRequest;
import com.educativo.api.dto.CreditHistoryResponse;
import com.educativo.api.dto.MessageResponse;
import com.educativo.api.dto.MonthlyFeeResponse;
import com.educativo.api.dto.ParentResponse;
import com.educativo.api.dto.StudentResponse;

import java.util.List;

public interface ParentService {
    ParentResponse getMyData(String username);
    List<StudentResponse> getMyChildren(String username);
    List<MonthlyFeeResponse> getStudentFees(String username, Long studentId);
    CardInfoResponse getMyCardInfo(String username);
    MessageResponse processCardPayment(String username, CardPaymentRequest request);
    byte[] generateReceipt(String username, Long feeId);
    CreditHistoryResponse getCreditHistory(String username);
    byte[] exportStudentHistory(String username, Long studentId);
}
