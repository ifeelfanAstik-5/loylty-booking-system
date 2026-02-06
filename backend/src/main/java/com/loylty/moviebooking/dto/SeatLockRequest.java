package com.loylty.moviebooking.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatLockRequest {
    
    @NotNull(message = "Show ID is required")
    private Long showId;
    
    @NotEmpty(message = "Seat IDs cannot be empty")
    private List<Long> seatIds;
    
    private String userId; // Can be session ID or user ID
}
