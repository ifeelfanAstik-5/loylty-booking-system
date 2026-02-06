package com.loylty.moviebooking.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long bookingId;
    private String guestName;
    private String guestEmail;
    private String movieTitle;
    private String cinemaName;
    private String screenName;
    private LocalDateTime showTime;
    private List<SeatDto> seats;
    private BigDecimal totalAmount;
    private LocalDateTime bookingTime;
    private String status;
}
