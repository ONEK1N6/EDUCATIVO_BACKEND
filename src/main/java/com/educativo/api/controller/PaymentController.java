package com.educativo.api.controller;

import com.educativo.api.dto.MonthlyFeeResponse;
import com.educativo.api.dto.PaymentSummaryResponse;
import com.educativo.api.service.PaymentService;
import com.educativo.api.dto.FeeConfigRequest;
import com.educativo.api.dto.ManualMonthlyFeeRequest;
import com.educativo.api.dto.MessageResponse;
import com.educativo.api.entity.EducationLevel;
import com.educativo.api.entity.FeeConfig;
import com.educativo.api.repository.FeeConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/payments")
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    @Autowired
    FeeConfigRepository feeConfigRepository;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentSummaryResponse> getSummary() {
        return ResponseEntity.ok(paymentService.getPaymentSummary());
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MonthlyFeeResponse>> getStudentFees(@PathVariable Long studentId) {
        return ResponseEntity.ok(paymentService.getFeesByStudent(studentId));
    }

    @PostMapping("/record/{feeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> recordPayment(@PathVariable Long feeId) {
        return ResponseEntity.ok(paymentService.recordPayment(feeId));
    }

    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FeeConfig>> getAllConfigs() {
        return ResponseEntity.ok(feeConfigRepository.findAll());
    }

    @PutMapping("/config/{level}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> updateConfig(@PathVariable EducationLevel level, @RequestBody FeeConfigRequest request) {
        FeeConfig config = feeConfigRepository.findByLevel(level)
                .orElse(FeeConfig.builder().level(level).build());

        config.setMonthlyAmount(request.getMonthlyAmount());
        config.setRegistrationAmount(request.getRegistrationAmount());
        config.setDueDay(request.getDueDay());
        config.setLastUpdated(LocalDateTime.now());

        feeConfigRepository.save(config);
        return ResponseEntity.ok(new MessageResponse("Configuración actualizada para " + level));
    }

    @PostMapping("/manual")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> createManualMonthlyFee(@RequestBody ManualMonthlyFeeRequest request) {
        return ResponseEntity.ok(paymentService.createManualMonthlyFee(request));
    }

    @PostMapping("/group")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> createGroupPayment(@RequestBody com.educativo.api.dto.GroupPaymentRequest request) {
        return ResponseEntity.ok(paymentService.createGroupPayment(request));
    }

    @GetMapping("/student/{studentId}/history/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportStudentHistory(@PathVariable Long studentId) {
        byte[] pdf = paymentService.generatePaymentHistoryPdf(studentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "historial_pagos_estudiante_" + studentId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}
