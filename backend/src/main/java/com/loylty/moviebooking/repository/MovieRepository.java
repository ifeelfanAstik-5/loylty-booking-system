package com.loylty.moviebooking.repository;

import com.loylty.moviebooking.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    
    @Query("SELECT DISTINCT m FROM Movie m " +
           "JOIN m.shows s " +
           "JOIN s.screen sc " +
           "JOIN sc.cinema c " +
           "WHERE c.city.id = :cityId " +
           "AND s.showTime > CURRENT_TIMESTAMP")
    List<Movie> findMoviesByCity(@Param("cityId") Long cityId);
    
    List<Movie> findByTitleContainingIgnoreCase(String title);
}
