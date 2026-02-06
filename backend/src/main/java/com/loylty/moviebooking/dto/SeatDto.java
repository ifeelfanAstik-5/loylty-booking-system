package com.loylty.moviebooking.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatDto {
    private Long id;
    private Integer rowNumber;
    private Integer seatNumber;
    private String category;
    private String status; // AVAILABLE, LOCKED, BOOKED
    private String lockUserId; // null if not locked
    private java.time.LocalDateTime lockExpiryTime; // null if not locked
}
