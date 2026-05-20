package com.educativo.api.service;

import com.educativo.api.dto.MessageResponse;
import com.educativo.api.dto.ParentRegistrationRequest;

import com.educativo.api.dto.ParentResponse;
import com.educativo.api.dto.StudentResponse;
import java.util.List;

import com.educativo.api.dto.ParentUpdateRequest;
import com.educativo.api.dto.StudentUpdateRequest;

public interface AdminService {
    MessageResponse registerParent(ParentRegistrationRequest request);
    List<ParentResponse> getAllParents();
    List<StudentResponse> getAllStudents();
    MessageResponse updateParent(Long id, ParentUpdateRequest request);
    MessageResponse updateStudent(Long id, StudentUpdateRequest request);
}
