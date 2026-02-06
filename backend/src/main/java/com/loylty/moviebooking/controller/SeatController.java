package com.loylty.moviebooking.controller;

import com.loylty.moviebooking.dto.SeatDto;
import com.loylty.moviebooking.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SeatController {
    
    private final SeatService seatService;
    
    @GetMapping("/show/{showId}/layout")
    public ResponseEntity<List<SeatDto>> getSeatLayout(@PathVariable Long showId) {
        return ResponseEntity.ok(seatService.getSeatLayout(showId));
    }
    
    @PostMapping("/show/{showId}/status")
    public ResponseEntity<List<SeatDto>> getSeatStatus(
            @PathVariable Long showId,
            @RequestBody List<Long> seatIds) {
        return ResponseEntity.ok(seatService.getSeatStatus(showId, seatIds));
    }
}
