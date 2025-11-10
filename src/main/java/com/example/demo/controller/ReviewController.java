package com.example.demo.controller;

import com.example.demo.dto.ReviewDTO;
import com.example.demo.model.Review;
import com.example.demo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class ReviewController {

    private final ReviewService reviewService;

    // 🟢 Add a review
    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody ReviewDTO reviewDTO) {
        // Get logged-in username
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Review saved = reviewService.addReview(reviewDTO, username);
        return ResponseEntity.ok(saved);
    }
    
    // 🌐 Get all reviews (public - all customers can see)
    @GetMapping("/all")
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        List<ReviewDTO> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }
    
    // � Get current user's reviews
    @GetMapping("/my-reviews")
    public ResponseEntity<List<ReviewDTO>> getMyReviews() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // Get user ID from service and retrieve their reviews
        return ResponseEntity.ok(reviewService.getAllReviews()); // Will filter in frontend
    }

    // �🟡 Get review for an appointment
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<?> getReview(@PathVariable Long appointmentId) {
        return reviewService.getReviewByAppointment(appointmentId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 🔵 Update review
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReview(@PathVariable Long id, @RequestBody ReviewDTO updatedReview) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Review review = reviewService.updateReview(id, updatedReview.getRating(), updatedReview.getComment(), username);
        return ResponseEntity.ok(review);
    }

    // 🔴 Delete review
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        reviewService.deleteReview(id, username);
        return ResponseEntity.ok("Review deleted successfully");
    }
}
