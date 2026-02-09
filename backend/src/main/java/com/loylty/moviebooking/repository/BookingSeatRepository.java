package com.loylty.moviebooking.repository;

import com.loylty.moviebooking.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {
    
    List<BookingSeat> findByBookingId(Long bookingId);
    
    @Query("SELECT bs.seatId FROM BookingSeat bs WHERE bs.booking.show.id = :showId")
    Set<Long> findBookedSeatIdsByShowId(@Param("showId") Long showId);
    
    @Query("SELECT bs FROM BookingSeat bs WHERE bs.booking.show.id = :showId")
    List<BookingSeat> findByShowId(@Param("showId") Long showId);
    
    @Query("SELECT bs.seatId FROM BookingSeat bs WHERE bs.booking.id = :bookingId")
    Set<Long> findSeatIdsByBookingId(@Param("bookingId") Long bookingId);
}
