package com.loylty.moviebooking.controller;

import com.loylty.moviebooking.entity.Show;
import com.loylty.moviebooking.entity.ShowSeat;
import com.loylty.moviebooking.service.ShowManagementService;
import com.loylty.moviebooking.service.ShowSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal API for show management
 * These endpoints are not exposed to end users
 */
@RestController
@RequestMapping("/internal/shows")
@RequiredArgsConstructor
public class ShowManagementController {
    
    private final ShowManagementService showManagementService;
    private final ShowSeatService showSeatService;
    
    /**
     * Create a new show with automatic seating plan generation
     * This is the internal API that ensures every show has seats
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createShow(@RequestBody Show show) {
        try {
            Show createdShow = showManagementService.createShow(show);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("showId", createdShow.getId());
            response.put("message", "Show created with seating plan");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error creating show: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get seating layout for a show
     */
    @GetMapping("/{showId}/seats")
    public ResponseEntity<List<ShowSeat>> getShowSeats(@PathVariable Long showId) {
        try {
            List<ShowSeat> seats = showSeatService.getShowSeats(showId);
            return ResponseEntity.ok(seats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Initialize seating plans for all existing shows
     * Use this for data migration
     */
    @PostMapping("/initialize-all-seating-plans")
    public ResponseEntity<Map<String, Object>> initializeAllSeatingPlans() {
        try {
            showManagementService.initializeSeatingPlansForExistingShows();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Seating plans initialized for all existing shows");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error initializing seating plans: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Clean up expired locks
     */
    @PostMapping("/cleanup-expired-locks")
    public ResponseEntity<Map<String, Object>> cleanupExpiredLocks() {
        try {
            int releasedCount = showManagementService.cleanupExpiredLocks();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("releasedCount", releasedCount);
            response.put("message", "Cleaned up " + releasedCount + " expired locks");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error cleaning up locks: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get seat availability statistics for a show
     */
    @GetMapping("/{showId}/availability")
    public ResponseEntity<Map<String, Object>> getSeatAvailability(@PathVariable Long showId) {
        try {
            long available = showSeatService.getAvailableSeatsCount(showId);
            long locked = showSeatService.getLockedSeatsCount(showId);
            long booked = showSeatService.getBookedSeatsCount(showId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("available", available);
            response.put("locked", locked);
            response.put("booked", booked);
            response.put("total", available + locked + booked);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
