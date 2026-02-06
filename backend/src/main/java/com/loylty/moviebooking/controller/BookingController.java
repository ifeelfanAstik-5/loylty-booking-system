package com.loylty.moviebooking.controller;

import com.loylty.moviebooking.dto.SeatLockRequest;
import com.loylty.moviebooking.dto.SeatLockResponse;
import com.loylty.moviebooking.dto.BookingRequest;
import com.loylty.moviebooking.dto.BookingResponse;
import com.loylty.moviebooking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class BookingController {
    
    private final BookingService bookingService;
    
    @PostMapping("/lock-seats")
    public ResponseEntity<SeatLockResponse> lockSeats(@Valid @RequestBody SeatLockRequest request) {
        return ResponseEntity.ok(bookingService.lockSeats(request));
    }
    
    @PostMapping("/unlock-seats")
    public ResponseEntity<SeatLockResponse> unlockSeats(@Valid @RequestBody SeatLockRequest request) {
        return ResponseEntity.ok(bookingService.unlockSeats(request));
    }
    
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmBooking(
            @RequestParam Long showId,
            @RequestParam java.util.List<Long> seatIds,
            @RequestParam String userId) {
        boolean confirmed = bookingService.confirmBooking(showId, seatIds, userId);
        
        return ResponseEntity.ok(Map.of(
                "success", confirmed,
                "message", confirmed ? "Booking confirmed successfully" : "Failed to confirm booking"
        ));
    }
    
    @PostMapping("/create")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }
}
