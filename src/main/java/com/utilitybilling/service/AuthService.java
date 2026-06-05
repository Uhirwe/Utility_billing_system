package com.utilitybilling.service;

import com.utilitybilling.dto.auth.*;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void changePassword(String email, ChangePasswordRequest request);
    void completeFirstLogin(FirstLoginPasswordRequest request);
    void updateProfile(String email, UpdateProfileRequest request);
}
