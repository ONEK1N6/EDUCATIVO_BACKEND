package com.educativo.api.service;

import com.educativo.api.dto.JwtResponse;
import com.educativo.api.dto.LoginRequest;

public interface AuthService {
    JwtResponse authenticateUser(LoginRequest loginRequest);
}
