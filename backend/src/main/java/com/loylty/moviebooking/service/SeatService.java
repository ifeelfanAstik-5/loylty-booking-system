package com.loylty.moviebooking.service;

import com.loylty.moviebooking.dto.SeatDto;
import com.loylty.moviebooking.entity.ShowSeat;
import com.loylty.moviebooking.repository.ShowRepository;
import com.loylty.moviebooking.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {
    
    private final ShowSeatRepository showSeatRepository;
    private final ShowRepository showRepository;
    
    public List<SeatDto> getSeatLayout(Long showId) {
        // Use the new ShowSeat system instead of legacy Seat system
        List<ShowSeat> showSeats = showSeatRepository.findByShowIdOrderByRowNumberAscSeatNumberAsc(showId);
        
        return showSeats.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private SeatDto convertToDto(ShowSeat showSeat) {
        return new SeatDto(
                showSeat.getId(),
                showSeat.getRowNumber(),
                showSeat.getSeatNumber(),
                showSeat.getCategory().name(),
                showSeat.getStatus().name(),
                showSeat.getLockUserId(),
                showSeat.getLockExpiryTime()
        );
    }
}
