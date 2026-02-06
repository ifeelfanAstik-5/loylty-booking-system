package com.loylty.moviebooking.cache;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface SeatLockService {
    
    /**
     * Lock seats for a specific show
     * @param showId the show ID
     * @param seatIds list of seat IDs to lock
     * @param userId the user identifier (can be session ID or user ID)
     * @param lockDurationMinutes duration in minutes for the lock
     * @return true if seats were successfully locked, false if any seat is already locked
     */
    boolean lockSeats(Long showId, List<Long> seatIds, String userId, int lockDurationMinutes);
    
    /**
     * Unlock seats for a specific show
     * @param showId the show ID
     * @param seatIds list of seat IDs to unlock
     * @param userId the user identifier (must match the one who locked the seats)
     * @return true if seats were successfully unlocked
     */
    boolean unlockSeats(Long showId, List<Long> seatIds, String userId);
    
    /**
     * Check if seats are available (not locked or booked)
     * @param showId the show ID
     * @param seatIds list of seat IDs to check
     * @return set of available seat IDs
     */
    Set<Long> getAvailableSeats(Long showId, List<Long> seatIds);
    
    /**
     * Get locked seats for a show
     * @param showId the show ID
     * @return set of locked seat IDs
     */
    Set<Long> getLockedSeats(Long showId);
    
    /**
     * Get lock information for specific seats
     * @param showId the show ID
     * @param seatIds list of seat IDs
     * @return lock information containing user ID and expiry time
     */
    SeatLockInfo getSeatLockInfo(Long showId, Long seatId);
    
    /**
     * Clean up expired locks
     * This method should be called periodically to remove expired locks
     */
    void cleanupExpiredLocks();
    
    /**
     * Confirm booking and permanently mark seats as booked
     * @param showId the show ID
     * @param seatIds list of seat IDs to book
     * @param userId the user identifier
     * @return true if booking was confirmed successfully
     */
    boolean confirmBooking(Long showId, List<Long> seatIds, String userId);
    
    /**
     * Inner class to hold lock information
     */
    class SeatLockInfo {
        private final String userId;
        private final LocalDateTime lockTime;
        private final LocalDateTime expiryTime;
        
        public SeatLockInfo(String userId, LocalDateTime lockTime, LocalDateTime expiryTime) {
            this.userId = userId;
            this.lockTime = lockTime;
            this.expiryTime = expiryTime;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public LocalDateTime getLockTime() {
            return lockTime;
        }
        
        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }
}
