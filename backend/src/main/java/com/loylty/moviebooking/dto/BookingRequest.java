package com.loylty.moviebooking.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    
    @NotNull(message = "Show ID is required")
    private Long showId;
    
    @NotBlank(message = "Guest name is required")
    private String guestName;
    
    @NotBlank(message = "Guest email is required")
    @Email(message = "Invalid email format")
    private String guestEmail;
    
    @NotEmpty(message = "Seat IDs cannot be empty")
    private List<Long> seatIds;
    
    private String userId; // User ID from seat lock
}
