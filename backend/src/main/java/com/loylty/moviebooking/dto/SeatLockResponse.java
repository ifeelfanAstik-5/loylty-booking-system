package com.loylty.moviebooking.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatLockResponse {
    private boolean success;
    private String message;
    private List<Long> lockedSeatIds;
    private LocalDateTime lockExpiryTime;
    private String lockUserId;
}
