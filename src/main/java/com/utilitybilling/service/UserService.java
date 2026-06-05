package com.utilitybilling.service;

import com.utilitybilling.dto.user.CreateUserRequest;
import com.utilitybilling.dto.user.CreateUserResponse;
import com.utilitybilling.dto.user.UpdateUserRoleRequest;
import com.utilitybilling.dto.user.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    CreateUserResponse createStaffUser(CreateUserRequest request, String actorEmail);
    UserResponse upgradeUserRole(Long id, UpdateUserRoleRequest request, String actorEmail);
    UserResponse revokeUserRole(Long id, String actorEmail);
    UserResponse getUserById(Long id);
    Page<UserResponse> getAllUsers(Pageable pageable);
}
