package com.clinic.service;

import com.clinic.dto.request.LoginRequest;
import com.clinic.dto.request.RegisterRequest;
import com.clinic.dto.response.AuthResponse;
import com.clinic.dto.response.UserSummaryResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserSummaryResponse getCurrentUserProfile();
}
