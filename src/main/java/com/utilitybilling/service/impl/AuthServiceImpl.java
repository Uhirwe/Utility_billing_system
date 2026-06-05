package com.utilitybilling.service.impl;

import com.utilitybilling.dto.auth.*;
import com.utilitybilling.entity.Customer;
import com.utilitybilling.entity.Role;
import com.utilitybilling.entity.User;
import com.utilitybilling.enums.RoleName;
import com.utilitybilling.enums.UserStatus;
import com.utilitybilling.exception.DuplicateResourceException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.exception.UnauthorizedException;
import com.utilitybilling.mapper.UserMapper;
import com.utilitybilling.repository.CustomerRepository;
import com.utilitybilling.repository.RoleRepository;
import com.utilitybilling.repository.UserRepository;
import com.utilitybilling.security.jwt.JwtTokenProvider;
import com.utilitybilling.service.AuthService;
import com.utilitybilling.service.EmailService;
import com.utilitybilling.util.AgeValidator;
import com.utilitybilling.util.ValidationConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        if (customerRepository.existsByNationalId(request.getNationalId())) {
            throw new DuplicateResourceException("National ID already registered");
        }
        if (customerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number already registered");
        }

        AgeValidator.validateMinimumAge(request.getDateOfBirth(), 18);

        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("ROLE_CUSTOMER not found"));

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
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .passwordExpired(false)
                .accountLocked(false)
                .roles(Set.of(customerRole))
                .build();
        userRepository.save(user);

        Customer customer = Customer.builder()
                .user(user)
                .fullNames(fullNames)
                .nationalId(request.getNationalId())
                .email(request.getEmail())
                .phoneCountryCode(countryCode)
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth())
                .build();
        customerRepository.save(customer);

        emailService.sendCustomerRegistrationEmail(user.getEmail(), fullNames);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        return buildAuthResponse(authentication, user, false);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        validateLoginEligibility(user);

        if (Boolean.TRUE.equals(user.getPasswordExpired())) {
            throw new UnauthorizedException(
                    "Temporary password expired. Use /auth/first-login/change-password before logging in.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        return buildAuthResponse(authentication, user, false);
    }

    @Override
    @Transactional
    public void completeFirstLogin(FirstLoginPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!Boolean.TRUE.equals(user.getPasswordExpired())) {
            throw new UnauthorizedException("First-login password change is not required for this account");
        }

        if (!passwordEncoder.matches(request.getTemporaryPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid temporary password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordExpired(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordExpired(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getFirstName() != null || request.getLastName() != null) {
            user.setFullNames(user.getFirstName() + " " + user.getLastName());
        }
        if (request.getPhoneCountryCode() != null) {
            user.setPhoneCountryCode(request.getPhoneCountryCode());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        userRepository.save(user);

        customerRepository.findByUserId(user.getId()).ifPresent(customer -> {
            if (request.getFirstName() != null || request.getLastName() != null) {
                customer.setFullNames(user.getFullNames());
            }
            if (request.getPhoneCountryCode() != null) {
                customer.setPhoneCountryCode(request.getPhoneCountryCode());
            }
            if (request.getPhoneNumber() != null) {
                customer.setPhoneNumber(request.getPhoneNumber());
            }
            customerRepository.save(customer);
        });
    }

    private void validateLoginEligibility(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is inactive");
        }
        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            throw new UnauthorizedException("Account is locked");
        }
    }

    private AuthResponse buildAuthResponse(Authentication authentication, User user, boolean passwordChangeRequired) {
        String token = jwtTokenProvider.generateToken(authentication);
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .passwordChangeRequired(passwordChangeRequired)
                .build();
    }
}
