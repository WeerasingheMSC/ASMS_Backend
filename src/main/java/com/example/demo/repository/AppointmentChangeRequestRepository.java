package com.example.demo.repository;

import com.example.demo.model.AppointmentChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentChangeRequestRepository extends JpaRepository<AppointmentChangeRequest, Long> {
    
    List<AppointmentChangeRequest> findByCustomerId(Long customerId);
    
    List<AppointmentChangeRequest> findByAppointmentId(Long appointmentId);
    
    List<AppointmentChangeRequest> findByStatusOrderByRequestedAtDesc(AppointmentChangeRequest.RequestStatus status);
    
    List<AppointmentChangeRequest> findAllByOrderByRequestedAtDesc();
    
    Optional<AppointmentChangeRequest> findByAppointmentIdAndStatus(Long appointmentId, AppointmentChangeRequest.RequestStatus status);
}
