package com.loylty.moviebooking.service;

import com.loylty.moviebooking.cache.SeatLockService;
import com.loylty.moviebooking.dto.SeatDto;
import com.loylty.moviebooking.entity.Seat;
import com.loylty.moviebooking.entity.Show;
import com.loylty.moviebooking.repository.BookingSeatRepository;
import com.loylty.moviebooking.repository.SeatRepository;
import com.loylty.moviebooking.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {
    
    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatLockService seatLockService;
    
    public List<SeatDto> getSeatLayout(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found with id: " + showId));
        
        List<Seat> seats = seatRepository.findByScreenIdOrdered(show.getScreen().getId());
        Set<Long> bookedSeatIds = bookingSeatRepository.findBookedSeatIdsByShowId(showId);
        Set<Long> lockedSeatIds = seatLockService.getLockedSeats(showId);
        
        return seats.stream()
                .map(seat -> convertToDto(seat, bookedSeatIds, lockedSeatIds, showId))
                .collect(Collectors.toList());
    }
    
    public List<SeatDto> getSeatStatus(Long showId, List<Long> seatIds) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found with id: " + showId));
        
        List<Seat> seats = seatRepository.findByScreenIdAndSeatIds(show.getScreen().getId(), Set.copyOf(seatIds));
        Set<Long> bookedSeatIds = bookingSeatRepository.findBookedSeatIdsByShowId(showId);
        Set<Long> lockedSeatIds = seatLockService.getLockedSeats(showId);
        
        return seats.stream()
                .map(seat -> convertToDto(seat, bookedSeatIds, lockedSeatIds, showId))
                .collect(Collectors.toList());
    }
    
    private SeatDto convertToDto(Seat seat, Set<Long> bookedSeatIds, Set<Long> lockedSeatIds, Long showId) {
        String status;
        String lockUserId = null;
        java.time.LocalDateTime lockExpiryTime = null;
        
        if (bookedSeatIds.contains(seat.getId())) {
            status = "BOOKED";
        } else if (lockedSeatIds.contains(seat.getId())) {
            status = "LOCKED";
            SeatLockService.SeatLockInfo lockInfo = seatLockService.getSeatLockInfo(showId, seat.getId());
            if (lockInfo != null) {
                lockUserId = lockInfo.getUserId();
                lockExpiryTime = lockInfo.getExpiryTime();
            }
        } else {
            status = "AVAILABLE";
        }
        
        return new SeatDto(
                seat.getId(),
                seat.getRowNumber(),
                seat.getSeatNumber(),
                seat.getCategory().name(),
                status,
                lockUserId,
                lockExpiryTime
        );
    }
}
