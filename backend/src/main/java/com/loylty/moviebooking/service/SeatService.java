package com.loylty.moviebooking.service;

import com.loylty.moviebooking.dto.SeatDto;
import com.loylty.moviebooking.entity.Seat;
import com.loylty.moviebooking.entity.Show;
import com.loylty.moviebooking.repository.BookingSeatRepository;
import com.loylty.moviebooking.repository.SeatRepository;
import com.loylty.moviebooking.repository.ShowRepository;
import com.loylty.moviebooking.cache.SeatLockService;
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
        try {
            System.out.println("=== DEBUG: getSeatLayout called for showId: " + showId);
            
            // Validate show exists
            showRepository.findById(showId)
                    .orElseThrow(() -> new RuntimeException("Show not found with id: " + showId));
            
            System.out.println("Show exists, proceeding with seat layout generation");
            
            // Generate a standard 10x12 seat layout (120 seats)
            // This matches the seat IDs that work with the locking system
            Set<Long> bookedSeatIds;
            try {
                bookedSeatIds = bookingSeatRepository.findBookedSeatIdsByShowId(showId);
                System.out.println("Booked seat IDs: " + bookedSeatIds);
            } catch (Exception e) {
                System.out.println("Error getting booked seats: " + e.getMessage());
                bookedSeatIds = java.util.Collections.emptySet();
            }
            
            Set<Long> lockedSeatIds;
            try {
                lockedSeatIds = seatLockService.getLockedSeats(showId);
                System.out.println("Locked seat IDs: " + lockedSeatIds);
            } catch (Exception e) {
                System.out.println("Error getting locked seats: " + e.getMessage());
                lockedSeatIds = java.util.Collections.emptySet();
            }
            
            List<SeatDto> seatLayout = new java.util.ArrayList<>();
            
            // Generate seats 1-120 (10 rows x 12 seats)
            for (int row = 1; row <= 10; row++) {
                for (int seatNum = 1; seatNum <= 12; seatNum++) {
                    long seatId = ((row - 1) * 12) + seatNum;
                    
                    String status;
                    String lockUserId = null;
                    java.time.LocalDateTime lockExpiryTime = null;
                    
                    if (bookedSeatIds.contains(seatId)) {
                        status = "BOOKED";
                    } else if (lockedSeatIds.contains(seatId)) {
                        status = "LOCKED";
                        try {
                            SeatLockService.SeatLockInfo lockInfo = seatLockService.getSeatLockInfo(showId, seatId);
                            if (lockInfo != null) {
                                lockUserId = lockInfo.getUserId();
                                lockExpiryTime = lockInfo.getExpiryTime();
                            }
                        } catch (Exception e) {
                            System.out.println("Error getting lock info for seat " + seatId + ": " + e.getMessage());
                        }
                    } else {
                        status = "AVAILABLE";
                    }
                    
                    // Determine seat category (last 2 rows are premium)
                    String category = (row >= 9) ? "PREMIUM" : "REGULAR";
                    
                    seatLayout.add(new SeatDto(
                            seatId,
                            row,
                            seatNum,
                            category,
                            status,
                            lockUserId,
                            lockExpiryTime
                    ));
                }
            }
            
            System.out.println("Generated seat layout with " + seatLayout.size() + " seats");
            return seatLayout;
        } catch (Exception e) {
            System.out.println("ERROR in getSeatLayout: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to get seat layout: " + e.getMessage(), e);
        }
    }
}
