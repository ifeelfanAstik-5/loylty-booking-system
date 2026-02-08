package com.loylty.moviebooking.cache;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class InMemorySeatLockService implements SeatLockService {
    
    // Key: showId, Value: seatId -> LockInfo
    private final Map<Long, Map<Long, LockInfo>> seatLocks = new ConcurrentHashMap<>();
    
    // Key: showId, Value: set of booked seat IDs
    private final Map<Long, Set<Long>> bookedSeats = new ConcurrentHashMap<>();
    
    private ScheduledExecutorService cleanupExecutor;
    
    @PostConstruct
    public void init() {
        // Schedule cleanup of expired locks every minute
        cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredLocks, 1, 1, TimeUnit.MINUTES);
    }
    
    @PreDestroy
    public void destroy() {
        if (cleanupExecutor != null) {
            cleanupExecutor.shutdown();
        }
    }
    
    @Override
    public boolean lockSeats(Long showId, List<Long> seatIds, String userId, int lockDurationMinutes) {
        Map<Long, LockInfo> showLocks = seatLocks.computeIfAbsent(showId, k -> new ConcurrentHashMap<>());
        Set<Long> showBookedSeats = bookedSeats.computeIfAbsent(showId, k -> ConcurrentHashMap.newKeySet());
        
        // Check if any seats are already locked by different user or booked
        for (Long seatId : seatIds) {
            LockInfo existingLock = showLocks.get(seatId);
            if (existingLock != null && !existingLock.isExpired() && !existingLock.userId.equals(userId)) {
                return false; // Seat is locked by another user
            }
            if (showBookedSeats.contains(seatId)) {
                return false; // Seat is already booked
            }
        }
        
        // Lock all seats
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryTime = now.plusMinutes(lockDurationMinutes);
        
        for (Long seatId : seatIds) {
            showLocks.put(seatId, new LockInfo(userId, now, expiryTime));
        }
        
        return true;
    }
    
    @Override
    public boolean unlockSeats(Long showId, List<Long> seatIds, String userId) {
        Map<Long, LockInfo> showLocks = seatLocks.get(showId);
        if (showLocks == null) {
            return false;
        }
        
        boolean allUnlocked = true;
        for (Long seatId : seatIds) {
            LockInfo lockInfo = showLocks.get(seatId);
            if (lockInfo != null && lockInfo.userId.equals(userId)) {
                showLocks.remove(seatId);
            } else {
                allUnlocked = false;
            }
        }
        
        // Clean up empty show locks
        if (showLocks.isEmpty()) {
            seatLocks.remove(showId);
        }
        
        return allUnlocked;
    }
    
    @Override
    public Set<Long> getAvailableSeats(Long showId, List<Long> seatIds) {
        Map<Long, LockInfo> showLocks = seatLocks.get(showId);
        Set<Long> showBookedSeats = bookedSeats.get(showId);
        
        if (showLocks == null && showBookedSeats == null) {
            return new HashSet<>(seatIds);
        }
        
        return seatIds.stream()
                .filter(seatId -> {
                    // Check if seat is booked
                    if (showBookedSeats != null && showBookedSeats.contains(seatId)) {
                        return false;
                    }
                    // Check if seat is locked and not expired
                    if (showLocks != null) {
                        LockInfo lockInfo = showLocks.get(seatId);
                        return lockInfo == null || lockInfo.isExpired();
                    }
                    return true;
                })
                .collect(Collectors.toSet());
    }
    
    @Override
    public Set<Long> getLockedSeats(Long showId) {
        Map<Long, LockInfo> showLocks = seatLocks.get(showId);
        if (showLocks == null) {
            return Collections.emptySet();
        }
        
        LocalDateTime now = LocalDateTime.now();
        return showLocks.entrySet().stream()
                .filter(entry -> !entry.getValue().isExpired())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
    
    @Override
    public SeatLockInfo getSeatLockInfo(Long showId, Long seatId) {
        Map<Long, LockInfo> showLocks = seatLocks.get(showId);
        if (showLocks == null) {
            return null;
        }
        
        LockInfo lockInfo = showLocks.get(seatId);
        if (lockInfo == null || lockInfo.isExpired()) {
            return null;
        }
        
        return new SeatLockInfo(lockInfo.userId, lockInfo.lockTime, lockInfo.expiryTime);
    }
    
    @Override
    public void cleanupExpiredLocks() {
        LocalDateTime now = LocalDateTime.now();
        
        seatLocks.forEach((showId, showLocks) -> {
            showLocks.entrySet().removeIf(entry -> entry.getValue().isExpired());
        });
        
        // Remove empty show lock maps
        seatLocks.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
    
    @Override
    public boolean confirmBooking(Long showId, List<Long> seatIds, String userId) {
        Map<Long, LockInfo> showLocks = seatLocks.get(showId);
        Set<Long> showBookedSeats = bookedSeats.computeIfAbsent(showId, k -> ConcurrentHashMap.newKeySet());
        
        // Verify all seats are locked by this user and not expired
        if (showLocks != null) {
            for (Long seatId : seatIds) {
                LockInfo lockInfo = showLocks.get(seatId);
                if (lockInfo == null || !lockInfo.userId.equals(userId) || lockInfo.isExpired()) {
                    return false;
                }
            }
        }
        
        // Mark seats as booked
        showBookedSeats.addAll(seatIds);
        
        // Remove locks
        if (showLocks != null) {
            for (Long seatId : seatIds) {
                showLocks.remove(seatId);
            }
            if (showLocks.isEmpty()) {
                seatLocks.remove(showId);
            }
        }
        
        return true;
    }
    
    // Internal class to store lock information
    private static class LockInfo {
        final String userId;
        final LocalDateTime lockTime;
        final LocalDateTime expiryTime;
        
        LockInfo(String userId, LocalDateTime lockTime, LocalDateTime expiryTime) {
            this.userId = userId;
            this.lockTime = lockTime;
            this.expiryTime = expiryTime;
        }
        
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }
}
