package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeRequestDTO {
    private Long id;
    private Long appointmentId;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String reason;
    private String status;
    private String adminResponse;
    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;
    
    // Appointment details
    private String serviceType;
    private String appointmentDate;
    private String timeSlot;
    private String vehicleBrand;
    private String vehicleModel;
}
