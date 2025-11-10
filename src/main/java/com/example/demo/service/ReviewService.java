package com.example.demo.service;

import com.example.demo.dto.ReviewDTO;
import com.example.demo.model.Review;
import com.example.demo.model.Appointment;
import com.example.demo.model.AppointmentStatus;
import com.example.demo.model.User;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    public Review addReview(ReviewDTO dto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You cannot review someone else's appointment");
        }

        if (!appointment.getStatus().equals(AppointmentStatus.COMPLETED)) {
            throw new RuntimeException("You can only review completed appointments");
        }

        Review review = Review.builder()
                .rating(dto.getRating())
                .comment(dto.getComment())
                .appointmentId(dto.getAppointmentId())
                .userId(user.getId())
                .build();

        return reviewRepository.save(review);
    }

    public Optional<Review> getReviewByAppointment(Long appointmentId) {
        return reviewRepository.findByAppointmentId(appointmentId);
    }
    
    public List<ReviewDTO> getAllReviews() {
        List<Review> reviews = reviewRepository.findAllOrderByCreatedAtDesc();
        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<ReviewDTO> getReviewsByUser(Long userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        return reviews.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Review updateReview(Long id, int rating, String comment, String username) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!review.getUserId().equals(user.getId())) {
            throw new RuntimeException("You can only update your own reviews");
        }

        review.setRating(rating);
        review.setComment(comment);
        return reviewRepository.save(review);
    }

    public void deleteReview(Long id, String username) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!review.getUserId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own reviews");
        }
        
        reviewRepository.deleteById(id);
    }
    
    private ReviewDTO convertToDTO(Review review) {
        User user = userRepository.findById(review.getUserId()).orElse(null);
        
        return ReviewDTO.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .appointmentId(review.getAppointmentId())
                .username(user != null ? user.getUsername() : "Anonymous")
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
