package com.educativo.api.controller;

import com.educativo.api.dto.MessageResponse;
import com.educativo.api.dto.ParentRegistrationRequest;
import com.educativo.api.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.educativo.api.dto.ParentResponse;
import com.educativo.api.dto.StudentResponse;
import java.util.List;

import com.educativo.api.dto.ParentUpdateRequest;
import com.educativo.api.dto.StudentUpdateRequest;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    AdminService adminService;

    @PostMapping("/register-parent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> registerParent(@Valid @RequestBody ParentRegistrationRequest request) {
        MessageResponse response = adminService.registerParent(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/parents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ParentResponse>> getAllParents() {
        return ResponseEntity.ok(adminService.getAllParents());
    }

    @GetMapping("/students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        return ResponseEntity.ok(adminService.getAllStudents());
    }

    @PutMapping("/parents/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> updateParent(@PathVariable Long id, @Valid @RequestBody ParentUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateParent(id, request));
    }

    @PutMapping("/students/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateStudent(id, request));
    }
}
