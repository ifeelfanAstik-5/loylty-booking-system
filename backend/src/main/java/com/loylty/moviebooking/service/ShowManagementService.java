package com.loylty.moviebooking.service;

import com.loylty.moviebooking.entity.Show;
import com.loylty.moviebooking.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowManagementService {
    
    private final ShowRepository showRepository;
    private final ShowSeatService showSeatService;
    
    /**
     * Create a new show with seating plan
     * This is the internal API for show creation
     */
    @Transactional
    public Show createShow(Show show) {
        // Validate show has required screen information
        if (show.getScreen() == null || show.getScreen().getId() == null) {
            throw new IllegalArgumentException("Show must have a valid screen");
        }
        
        // Save the show first
        Show savedShow = showRepository.save(show);
        log.info("Created show: {} for movie: {} in screen: {}", 
            savedShow.getId(), savedShow.getMovie().getTitle(), savedShow.getScreen().getName());
        
        // Initialize seating plan for the show
        showSeatService.initializeSeatingPlan(savedShow.getId());
        
        return savedShow;
    }
    
    /**
     * Initialize seating plans for all existing shows
     * Use this for data migration
     */
    @Transactional
    public void initializeSeatingPlansForExistingShows() {
        List<Show> shows = showRepository.findAll();
        
        log.info("Initializing seating plans for {} existing shows", shows.size());
        
        for (Show show : shows) {
            try {
                showSeatService.initializeSeatingPlan(show.getId());
                log.info("Initialized seating plan for show: {}", show.getId());
            } catch (Exception e) {
                log.error("Failed to initialize seating plan for show {}: {}", show.getId(), e.getMessage());
            }
        }
    }
    
    /**
     * Clean up expired locks
     * This should be called periodically
     */
    public int cleanupExpiredLocks() {
        return showSeatService.releaseExpiredLocks();
    }
}
