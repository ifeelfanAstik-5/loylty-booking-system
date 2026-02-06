package com.loylty.moviebooking.repository;

import com.loylty.moviebooking.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {
    
    @Query("SELECT s FROM Show s " +
           "JOIN FETCH s.movie m " +
           "JOIN FETCH s.screen sc " +
           "JOIN FETCH sc.cinema c " +
           "JOIN FETCH c.theaterChain " +
           "WHERE c.city.id = :cityId " +
           "AND s.showTime > CURRENT_TIMESTAMP " +
           "ORDER BY s.showTime")
    List<Show> findShowsByCity(@Param("cityId") Long cityId);
    
    @Query("SELECT s FROM Show s " +
           "JOIN FETCH s.movie m " +
           "JOIN FETCH s.screen sc " +
           "JOIN FETCH sc.cinema c " +
           "JOIN FETCH c.theaterChain " +
           "WHERE m.id = :movieId " +
           "AND c.city.id = :cityId " +
           "AND s.showTime > CURRENT_TIMESTAMP " +
           "ORDER BY s.showTime")
    List<Show> findShowsByMovieAndCity(@Param("movieId") Long movieId, @Param("cityId") Long cityId);
    
    @Query("SELECT s FROM Show s " +
           "JOIN FETCH s.movie m " +
           "JOIN FETCH s.screen sc " +
           "JOIN FETCH sc.cinema c " +
           "JOIN FETCH c.theaterChain " +
           "WHERE m.id = :movieId " +
           "AND c.city.id = :cityId " +
           "AND s.showTime > :startTime " +
           "ORDER BY s.showTime")
    List<Show> findShowsByMovieAndCityAfter(@Param("movieId") Long movieId, 
                                           @Param("cityId") Long cityId, 
                                           @Param("startTime") LocalDateTime startTime);
    
    List<Show> findByScreenId(Long screenId);
    
    @Query("SELECT s FROM Show s WHERE s.showTime < :thresholdTime")
    List<Show> findPastShows(@Param("thresholdTime") LocalDateTime thresholdTime);
}
