package com.loylty.moviebooking.repository;

import com.loylty.moviebooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByShowId(Long showId);
    
    @Query("SELECT b FROM Booking b WHERE b.show.id = :showId AND b.guestEmail = :email")
    List<Booking> findByShowIdAndEmail(@Param("showId") Long showId, @Param("email") String email);
    
    @Query("SELECT b FROM Booking b WHERE b.guestEmail = :email ORDER BY b.bookingTime DESC")
    List<Booking> findByGuestEmail(@Param("email") String email);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.show.id = :showId")
    Long countBookingsByShowId(@Param("showId") Long showId);
    
    @Query("SELECT b FROM Booking b WHERE b.bookingTime >= :since ORDER BY b.bookingTime DESC")
    List<Booking> findRecentBookings(@Param("since") LocalDateTime since);
}
