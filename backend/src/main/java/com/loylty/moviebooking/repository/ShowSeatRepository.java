package com.loylty.moviebooking.repository;

import com.loylty.moviebooking.entity.ShowSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {
    
    List<ShowSeat> findByShowIdOrderByRowNumberAscSeatNumberAsc(Long showId);
    
    List<ShowSeat> findByShowIdAndStatus(Long showId, ShowSeat.SeatStatus status);
    
    List<ShowSeat> findByShowIdAndRowNumberAndSeatNumber(Long showId, Integer rowNumber, Integer seatNumber);
    
    List<ShowSeat> findByShowIdAndLockUserIdAndLockExpiryTimeAfter(
        Long showId, String userId, LocalDateTime now);
    
    @Query("SELECT s FROM ShowSeat s WHERE s.show.id = :showId AND s.id IN :seatIds")
    List<ShowSeat> findByShowIdAndIdIn(@Param("showId") Long showId, @Param("seatIds") List<Long> seatIds);
    
    @Modifying
    @Query("UPDATE ShowSeat s SET s.status = :availableStatus, s.lockUserId = NULL, s.lockExpiryTime = NULL WHERE s.lockExpiryTime < :now")
    int releaseExpiredLocks(@Param("availableStatus") ShowSeat.SeatStatus availableStatus, @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(s) FROM ShowSeat s WHERE s.show.id = :showId AND s.status = :status")
    long countByShowIdAndStatus(@Param("showId") Long showId, @Param("status") ShowSeat.SeatStatus status);
    
    @Query("SELECT s FROM ShowSeat s WHERE s.show.id = :showId AND s.rowNumber = :rowNumber AND s.seatNumber = :seatNumber")
    Optional<ShowSeat> findByShowIdAndPosition(@Param("showId") Long showId, @Param("rowNumber") Integer rowNumber, @Param("seatNumber") Integer seatNumber);
}
