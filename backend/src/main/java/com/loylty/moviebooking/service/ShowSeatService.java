package com.loylty.moviebooking.service;

import com.loylty.moviebooking.entity.Screen;
import com.loylty.moviebooking.entity.Show;
import com.loylty.moviebooking.entity.ShowSeat;
import com.loylty.moviebooking.repository.ScreenRepository;
import com.loylty.moviebooking.repository.ShowRepository;
import com.loylty.moviebooking.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowSeatService {
    
    private final ShowSeatRepository showSeatRepository;
    private final ShowRepository showRepository;
    private final ScreenRepository screenRepository;
    
    /**
     * Initialize seating plan for a new show
     * This is called internally when a show is created
     */
    @Transactional
    public void initializeSeatingPlan(Long showId) {
        Show show = showRepository.findById(showId)
            .orElseThrow(() -> new EntityNotFoundException("Show not found: " + showId));
        
        // Check if seating plan already exists
        List<ShowSeat> existingSeats = showSeatRepository.findByShowIdOrderByRowNumberAscSeatNumberAsc(showId);
        if (!existingSeats.isEmpty()) {
            log.warn("Seating plan already exists for show: {}", showId);
            return;
        }
        
        Screen screen = screenRepository.findById(show.getScreen().getId())
            .orElseThrow(() -> new EntityNotFoundException("Screen not found: " + show.getScreen().getId()));
        
        List<ShowSeat> showSeats = new ArrayList<>();
        
        // Create seats based on screen configuration
        for (int row = 1; row <= screen.getTotalRows(); row++) {
            for (int seatNum = 1; seatNum <= screen.getSeatsPerRow(); seatNum++) {
                ShowSeat.SeatCategory category = determineSeatCategory(row, screen.getTotalRows());
                BigDecimal price = category == ShowSeat.SeatCategory.PREMIUM ? 
                    show.getPremiumPrice() : show.getBasePrice();
                
                ShowSeat showSeat = ShowSeat.builder()
                    .show(show)
                    .rowNumber(row)
                    .seatNumber(seatNum)
                    .category(category)
                    .status(ShowSeat.SeatStatus.AVAILABLE)
                    .price(price)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
                
                showSeats.add(showSeat);
            }
        }
        
        showSeatRepository.saveAll(showSeats);
        log.info("Initialized seating plan for show {}: {} seats ({} rows × {} seats per row)", 
            showId, showSeats.size(), screen.getTotalRows(), screen.getSeatsPerRow());
    }
    
    /**
     * Get all seats for a show with their current status
     */
    public List<ShowSeat> getShowSeats(Long showId) {
        return showSeatRepository.findByShowIdOrderByRowNumberAscSeatNumberAsc(showId);
    }
    
    /**
     * Lock seats for booking
     */
    @Transactional
    public boolean lockSeats(Long showId, List<Integer> rowNumbers, List<Integer> seatNumbers, String userId, int lockDurationMinutes) {
        if (rowNumbers.size() != seatNumbers.size()) {
            throw new IllegalArgumentException("Row numbers and seat numbers must have same size");
        }
        
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(lockDurationMinutes);
        
        // Check and lock each seat
        for (int i = 0; i < rowNumbers.size(); i++) {
            Integer rowNumber = rowNumbers.get(i);
            Integer seatNumber = seatNumbers.get(i);
            
            ShowSeat showSeat = showSeatRepository.findByShowIdAndPosition(showId, rowNumber, seatNumber)
                .orElseThrow(() -> new EntityNotFoundException(
                    String.format("Seat not found: Row %d, Seat %d for Show %d", rowNumber, seatNumber, showId)));
            
            if (showSeat.getStatus() != ShowSeat.SeatStatus.AVAILABLE) {
                log.warn("Seat not available for locking: Row {}, Seat {}, Status: {}", 
                    rowNumber, seatNumber, showSeat.getStatus());
                return false;
            }
            
            // Lock the seat
            showSeat.setStatus(ShowSeat.SeatStatus.LOCKED);
            showSeat.setLockUserId(userId);
            showSeat.setLockExpiryTime(expiryTime);
            showSeat.setUpdatedAt(LocalDateTime.now());
        }
        
        log.info("Locked {} seats for show {} by user {}", rowNumbers.size(), showId, userId);
        return true;
    }
    
    /**
     * Book seats (update status from LOCKED to BOOKED)
     */
    @Transactional
    public void bookSeats(Long showId, List<Integer> rowNumbers, List<Integer> seatNumbers) {
        if (rowNumbers.size() != seatNumbers.size()) {
            throw new IllegalArgumentException("Row numbers and seat numbers must have same size");
        }
        
        for (int i = 0; i < rowNumbers.size(); i++) {
            Integer rowNumber = rowNumbers.get(i);
            Integer seatNumber = seatNumbers.get(i);
            
            ShowSeat showSeat = showSeatRepository.findByShowIdAndPosition(showId, rowNumber, seatNumber)
                .orElseThrow(() -> new EntityNotFoundException(
                    String.format("Seat not found: Row %d, Seat %d for Show %d", rowNumber, seatNumber, showId)));
            
            if (showSeat.getStatus() != ShowSeat.SeatStatus.LOCKED) {
                log.warn("Attempting to book seat that is not locked: Row {}, Seat {}, Status: {}", 
                    rowNumber, seatNumber, showSeat.getStatus());
                continue;
            }
            
            // Book the seat
            showSeat.setStatus(ShowSeat.SeatStatus.BOOKED);
            showSeat.setLockUserId(null);
            showSeat.setLockExpiryTime(null);
            showSeat.setUpdatedAt(LocalDateTime.now());
        }
        
        log.info("Booked {} seats for show {}", rowNumbers.size(), showId);
    }
    
    /**
     * Release expired locks
     */
    @Transactional
    public int releaseExpiredLocks() {
        int releasedCount = showSeatRepository.releaseExpiredLocks(
            ShowSeat.SeatStatus.AVAILABLE, LocalDateTime.now());
        
        if (releasedCount > 0) {
            log.info("Released {} expired seat locks", releasedCount);
        }
        
        return releasedCount;
    }
    
    /**
     * Get seat availability counts
     */
    public long getAvailableSeatsCount(Long showId) {
        return showSeatRepository.countByShowIdAndStatus(showId, ShowSeat.SeatStatus.AVAILABLE);
    }
    
    public long getLockedSeatsCount(Long showId) {
        return showSeatRepository.countByShowIdAndStatus(showId, ShowSeat.SeatStatus.LOCKED);
    }
    
    public long getBookedSeatsCount(Long showId) {
        return showSeatRepository.countByShowIdAndStatus(showId, ShowSeat.SeatStatus.BOOKED);
    }
    
    /**
     * Calculate total price for selected seats
     */
    public BigDecimal calculateTotalPrice(Long showId, List<Long> seatIds) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new EntityNotFoundException("Show not found: " + showId));
        
        // For simplicity, use base price for all seats
        // In a real system, you'd check seat categories
        return show.getBasePrice().multiply(BigDecimal.valueOf(seatIds.size()));
    }
    
    /**
     * Determine seat category based on row position
     */
    private ShowSeat.SeatCategory determineSeatCategory(int rowNumber, int totalRows) {
        // Last 3 rows are premium
        if (rowNumber > totalRows - 3) {
            return ShowSeat.SeatCategory.PREMIUM;
        }
        return ShowSeat.SeatCategory.REGULAR;
    }
}
