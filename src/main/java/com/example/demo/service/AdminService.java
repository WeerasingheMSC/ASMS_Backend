package com.example.demo.service;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.EmployeeRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public ApiResponse addEmployee(EmployeeRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(24);

        User employee = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Temporary random password
                .role(Role.EMPLOYEE)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .isActive(true)
                .isPasswordChanged(false)
                .resetToken(resetToken)
                .resetTokenExpiry(tokenExpiry)
                .build();

        userRepository.save(employee);

        // Send email with token
        try {
            emailService.sendEmployeeCreationEmail(employee.getEmail(), employee.getUsername(), resetToken);
        } catch (Exception e) {
            System.err.println("Failed to send email, but employee created: " + e.getMessage());
        }

        return ApiResponse.builder()
                .success(true)
                .message("Employee added successfully. Activation email sent.")
                .data(convertToUserResponse(employee))
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllEmployees() {
        return userRepository.findByRole(Role.EMPLOYEE).stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllCustomers() {
        return userRepository.findByRole(Role.CUSTOMER).stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToUserResponse(user);
    }

    @Transactional
    public ApiResponse deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Cannot deactivate admin user");
        }

        user.setIsActive(false);
        userRepository.save(user);

        return ApiResponse.builder()
                .success(true)
                .message("User deactivated successfully")
                .build();
    }

    @Transactional
    public ApiResponse activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setIsActive(true);
        userRepository.save(user);

        return ApiResponse.builder()
                .success(true)
                .message("User activated successfully")
                .build();
    }

    @Transactional
    public ApiResponse resendEmployeeActivationEmail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (user.getRole() != Role.EMPLOYEE) {
            throw new BadRequestException("User is not an employee");
        }

        // Generate new reset token
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(24);

        user.setResetToken(resetToken);
        user.setResetTokenExpiry(tokenExpiry);
        userRepository.save(user);

        // Resend email
        emailService.sendEmployeeCreationEmail(user.getEmail(), user.getUsername(), resetToken);

        return ApiResponse.builder()
                .success(true)
                .message("Activation email resent successfully")
                .build();
    }

    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .isActive(user.getIsActive())
                .isPasswordChanged(user.getIsPasswordChanged())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

