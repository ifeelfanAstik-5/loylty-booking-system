package com.loylty.moviebooking.repository;

import com.loylty.moviebooking.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    List<Seat> findByScreenId(Long screenId);
    
    List<Seat> findByScreenIdOrderByRowNumberAscSeatNumberAsc(Long screenId);
    
    @Query("SELECT s FROM Seat s WHERE s.screen.id = :screenId ORDER BY s.rowNumber, s.seatNumber")
    List<Seat> findByScreenIdOrdered(@Param("screenId") Long screenId);
    
    @Query("SELECT s FROM Seat s WHERE s.screen.id = :screenId AND s.id IN :seatIds ORDER BY s.rowNumber, s.seatNumber")
    List<Seat> findByScreenIdAndSeatIds(@Param("screenId") Long screenId, @Param("seatIds") Set<Long> seatIds);
    
    @Query("SELECT s FROM Seat s WHERE s.screen.id = :screenId AND s.rowNumber = :rowNumber AND s.seatNumber = :seatNumber")
    Seat findByScreenIdAndPosition(@Param("screenId") Long screenId, @Param("rowNumber") Integer rowNumber, @Param("seatNumber") Integer seatNumber);
}
