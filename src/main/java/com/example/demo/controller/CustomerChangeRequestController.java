package com.example.demo.controller;

import com.example.demo.dto.ChangeRequestDTO;
import com.example.demo.model.AppointmentChangeRequest;
import com.example.demo.service.AppointmentChangeRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/change-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerChangeRequestController {

    private final AppointmentChangeRequestService changeRequestService;

    @PostMapping
    public ResponseEntity<?> createChangeRequest(@RequestBody Map<String, Object> request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Long appointmentId = Long.valueOf(request.get("appointmentId").toString());
        String reason = request.get("reason").toString();

        try {
            AppointmentChangeRequest changeRequest = changeRequestService.createChangeRequest(
                    appointmentId, reason, username
            );
            return ResponseEntity.ok(changeRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<ChangeRequestDTO>> getMyRequests() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ChangeRequestDTO> requests = changeRequestService.getCustomerRequests(username);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/can-edit/{appointmentId}")
    public ResponseEntity<Map<String, Boolean>> canEditAppointment(@PathVariable Long appointmentId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean canEdit = changeRequestService.canEditAppointment(appointmentId, username);
        return ResponseEntity.ok(Map.of("canEdit", canEdit));
    }
}
