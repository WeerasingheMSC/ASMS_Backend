package com.example.demo.controller;

import com.example.demo.dto.ChangeRequestDTO;
import com.example.demo.model.AppointmentChangeRequest;
import com.example.demo.service.AppointmentChangeRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/change-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminChangeRequestController {

    private final AppointmentChangeRequestService changeRequestService;

    @GetMapping("/pending")
    public ResponseEntity<List<ChangeRequestDTO>> getPendingRequests() {
        List<ChangeRequestDTO> requests = changeRequestService.getAllPendingRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ChangeRequestDTO>> getAllRequests() {
        List<ChangeRequestDTO> requests = changeRequestService.getAllRequests();
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/{requestId}/approve")
    public ResponseEntity<?> approveRequest(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> body
    ) {
        try {
            String adminResponse = body.getOrDefault("adminResponse", "Approved");
            AppointmentChangeRequest request = changeRequestService.approveRequest(requestId, adminResponse);
            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable Long requestId,
            @RequestBody Map<String, String> body
    ) {
        try {
            String adminResponse = body.getOrDefault("adminResponse", "Rejected");
            AppointmentChangeRequest request = changeRequestService.rejectRequest(requestId, adminResponse);
            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
