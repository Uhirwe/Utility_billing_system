package com.utilitybilling.service.impl;

import com.utilitybilling.dto.email.EmailDeliveryResult;
import com.utilitybilling.dto.user.CreateUserRequest;
import com.utilitybilling.dto.user.CreateUserResponse;
import com.utilitybilling.dto.user.UpdateUserRoleRequest;
import com.utilitybilling.dto.user.UserResponse;
import com.utilitybilling.entity.Role;
import com.utilitybilling.entity.User;
import com.utilitybilling.enums.AuditActionType;
import com.utilitybilling.enums.RoleName;
import com.utilitybilling.enums.UserStatus;
import com.utilitybilling.exception.BusinessRuleException;
import com.utilitybilling.exception.DuplicateResourceException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.mapper.UserMapper;
import com.utilitybilling.repository.RoleRepository;
import com.utilitybilling.repository.UserRepository;
import com.utilitybilling.service.AuditService;
import com.utilitybilling.service.EmailService;
import com.utilitybilling.service.UserService;
import com.utilitybilling.util.PageableSanitizer;
import com.utilitybilling.util.PasswordGenerator;
import com.utilitybilling.util.ValidationConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final AuditService auditService;

    @Value("${app.login-url:http://localhost:8080/api/swagger-ui.html}")
    private String loginUrl;

    @Override
    @Transactional
    public CreateUserResponse createStaffUser(CreateUserRequest request, String actorEmail) {
        if (request.getRole() == RoleName.ROLE_CUSTOMER) {
            throw new BusinessRuleException("Customers self-register via /auth/register. Admin can create ADMIN, OPERATOR, or FINANCE users");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));

        String tempPassword = PasswordGenerator.generateTemporaryPassword();
        String fullNames = request.getFirstName() + " " + request.getLastName();
        String countryCode = request.getPhoneCountryCode() != null
                ? request.getPhoneCountryCode() : ValidationConstants.DEFAULT_COUNTRY_CODE;

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .fullNames(fullNames)
                .email(request.getEmail())
                .phoneCountryCode(countryCode)
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(tempPassword))
                .status(UserStatus.ACTIVE)
                .passwordExpired(true)
                .accountLocked(false)
                .roles(Set.of(role))
                .build();

        userRepository.save(user);

        EmailDeliveryResult emailResult = emailService.sendWelcomeEmail(
                user.getEmail(), fullNames, tempPassword, request.getRole(), loginUrl);

        auditService.log(actorEmail, AuditActionType.USER_CREATED, "User", user.getId(), null,
                "email=" + user.getEmail() + ", role=" + request.getRole() + ", emailSent=" + emailResult.isSent());

        return CreateUserResponse.builder()
                .user(userMapper.toResponse(user))
                .emailDelivery(emailResult)
                .build();
    }

    @Override
    @Transactional
    public UserResponse upgradeUserRole(Long id, UpdateUserRoleRequest request, String actorEmail) {
        User user = findUser(id);
        validateNotSeededAdmin(user);

        if (request.getRole() == RoleName.ROLE_CUSTOMER) {
            throw new BusinessRuleException("Use revoke role endpoint to assign ROLE_CUSTOMER");
        }

        String oldRoles = user.getRoles().stream().map(r -> r.getName().name()).reduce((a, b) -> a + "," + b).orElse("");

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));
        user.setRoles(Set.of(role));
        userRepository.save(user);

        emailService.sendRoleUpdateEmail(user.getEmail(), user.getFullNames(), request.getRole());
        auditService.log(actorEmail, AuditActionType.ROLE_CHANGED, "User", user.getId(), oldRoles, request.getRole().name());

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse revokeUserRole(Long id, String actorEmail) {
        User user = findUser(id);
        validateNotSeededAdmin(user);

        String oldRoles = user.getRoles().stream().map(r -> r.getName().name()).reduce((a, b) -> a + "," + b).orElse("");

        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("ROLE_CUSTOMER not found"));
        user.setRoles(Set.of(customerRole));
        userRepository.save(user);

        emailService.sendRoleRevokedEmail(user.getEmail(), user.getFullNames());
        auditService.log(actorEmail, AuditActionType.ROLE_CHANGED, "User", user.getId(), oldRoles, RoleName.ROLE_CUSTOMER.name());

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserById(Long id) {
        return userMapper.toResponse(findUser(id));
    }

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Pageable safePageable = PageableSanitizer.forUser(pageable);
        return userRepository.findAll(safePageable).map(userMapper::toResponse);
    }

    private void validateNotSeededAdmin(User user) {
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleName.ROLE_ADMIN);
        if (isAdmin) {
            throw new BusinessRuleException("Cannot modify the seeded admin account role");
        }
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}
