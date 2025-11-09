package com.example.demo.service;

import com.example.demo.dto.ChangeRequestDTO;
import com.example.demo.model.Appointment;
import com.example.demo.model.AppointmentChangeRequest;
import com.example.demo.model.User;
import com.example.demo.repository.AppointmentChangeRequestRepository;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentChangeRequestService {

    private final AppointmentChangeRequestRepository changeRequestRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public AppointmentChangeRequest createChangeRequest(Long appointmentId, String reason, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only request changes for your own appointments");
        }

        // Check if appointment is in editable state (PENDING, CONFIRMED)
        String status = appointment.getStatus().name();
        if (!status.equals("PENDING") && !status.equals("CONFIRMED")) {
            throw new RuntimeException("Cannot request changes for appointments in " + status + " state");
        }

        // Check if there's already a pending request
        changeRequestRepository.findByAppointmentIdAndStatus(
                appointmentId, 
                AppointmentChangeRequest.RequestStatus.PENDING
        ).ifPresent(req -> {
            throw new RuntimeException("There is already a pending change request for this appointment");
        });

        AppointmentChangeRequest request = AppointmentChangeRequest.builder()
                .appointmentId(appointmentId)
                .customerId(user.getId())
                .reason(reason)
                .status(AppointmentChangeRequest.RequestStatus.PENDING)
                .build();

        return changeRequestRepository.save(request);
    }

    public List<ChangeRequestDTO> getCustomerRequests(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<AppointmentChangeRequest> requests = changeRequestRepository.findByCustomerId(user.getId());
        return requests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ChangeRequestDTO> getAllPendingRequests() {
        List<AppointmentChangeRequest> requests = changeRequestRepository.findByStatusOrderByRequestedAtDesc(
                AppointmentChangeRequest.RequestStatus.PENDING
        );
        return requests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ChangeRequestDTO> getAllRequests() {
        List<AppointmentChangeRequest> requests = changeRequestRepository.findAllByOrderByRequestedAtDesc();
        return requests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AppointmentChangeRequest approveRequest(Long requestId, String adminResponse) {
        AppointmentChangeRequest request = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != AppointmentChangeRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Request has already been processed");
        }

        request.setStatus(AppointmentChangeRequest.RequestStatus.APPROVED);
        request.setAdminResponse(adminResponse);
        request.setRespondedAt(LocalDateTime.now());

        return changeRequestRepository.save(request);
    }

    public AppointmentChangeRequest rejectRequest(Long requestId, String adminResponse) {
        AppointmentChangeRequest request = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != AppointmentChangeRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Request has already been processed");
        }

        request.setStatus(AppointmentChangeRequest.RequestStatus.REJECTED);
        request.setAdminResponse(adminResponse);
        request.setRespondedAt(LocalDateTime.now());

        return changeRequestRepository.save(request);
    }

    public boolean canEditAppointment(Long appointmentId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getUser().getId().equals(user.getId())) {
            return false;
        }

        // Check if there's an approved change request
        return changeRequestRepository.findByAppointmentIdAndStatus(
                appointmentId,
                AppointmentChangeRequest.RequestStatus.APPROVED
        ).isPresent();
    }

    private ChangeRequestDTO convertToDTO(AppointmentChangeRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId()).orElse(null);
        User customer = userRepository.findById(request.getCustomerId()).orElse(null);

        ChangeRequestDTO dto = ChangeRequestDTO.builder()
                .id(request.getId())
                .appointmentId(request.getAppointmentId())
                .customerId(request.getCustomerId())
                .reason(request.getReason())
                .status(request.getStatus().name())
                .adminResponse(request.getAdminResponse())
                .requestedAt(request.getRequestedAt())
                .respondedAt(request.getRespondedAt())
                .build();

        if (customer != null) {
            dto.setCustomerName(customer.getUsername());
            dto.setCustomerEmail(customer.getEmail());
        }

        if (appointment != null) {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            dto.setServiceType(appointment.getServiceType());
            dto.setAppointmentDate(appointment.getAppointmentDate().format(dateFormatter));
            dto.setTimeSlot(appointment.getTimeSlot());
            dto.setVehicleBrand(appointment.getVehicleBrand());
            dto.setVehicleModel(appointment.getModel());
        }

        return dto;
    }
}
