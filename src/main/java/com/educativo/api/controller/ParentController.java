package com.educativo.api.controller;

import com.educativo.api.dto.CardInfoResponse;
import com.educativo.api.dto.CardPaymentRequest;
import com.educativo.api.dto.CreditHistoryResponse;
import com.educativo.api.dto.MessageResponse;
import com.educativo.api.dto.MonthlyFeeResponse;
import com.educativo.api.dto.ParentResponse;
import com.educativo.api.dto.StudentResponse;
import com.educativo.api.service.ParentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/parent")
public class ParentController {

    @Autowired
    ParentService parentService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ParentResponse> getMyData(Principal principal) {
        return ResponseEntity.ok(parentService.getMyData(principal.getName()));
    }

    @GetMapping("/children")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<StudentResponse>> getMyChildren(Principal principal) {
        return ResponseEntity.ok(parentService.getMyChildren(principal.getName()));
    }

    @GetMapping("/fees/{studentId}")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<MonthlyFeeResponse>> getStudentFees(Principal principal, @PathVariable Long studentId) {
        return ResponseEntity.ok(parentService.getStudentFees(principal.getName(), studentId));
    }

    @GetMapping("/card")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<CardInfoResponse> getMyCard(Principal principal) {
        return ResponseEntity.ok(parentService.getMyCardInfo(principal.getName()));
    }

    @PostMapping("/pay")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<MessageResponse> processPayment(Principal principal, @RequestBody CardPaymentRequest request) {
        return ResponseEntity.ok(parentService.processCardPayment(principal.getName(), request));
    }

    @GetMapping("/receipt/{feeId}")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<byte[]> downloadReceipt(Principal principal, @PathVariable Long feeId) {
        byte[] pdf = parentService.generateReceipt(principal.getName(), feeId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "comprobante_" + feeId + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @GetMapping("/credit-history")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<CreditHistoryResponse> getCreditHistory(Principal principal) {
        return ResponseEntity.ok(parentService.getCreditHistory(principal.getName()));
    }

    @GetMapping("/fees/{studentId}/history/export")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<byte[]> exportStudentHistory(Principal principal, @PathVariable Long studentId) {
        byte[] pdf = parentService.exportStudentHistory(principal.getName(), studentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "historial_pagos_" + studentId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}
