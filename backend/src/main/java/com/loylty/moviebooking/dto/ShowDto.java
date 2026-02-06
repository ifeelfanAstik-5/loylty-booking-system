package com.loylty.moviebooking.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowDto {
    private Long id;
    private MovieDto movie;
    private CinemaDto cinema;
    private String screenName;
    private LocalDateTime showTime;
    private LocalDateTime endTime;
    private BigDecimal basePrice;
    private BigDecimal premiumPrice;
}
