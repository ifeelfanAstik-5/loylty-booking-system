package com.loylty.moviebooking.repository;

import com.loylty.moviebooking.entity.Cinema;
import com.loylty.moviebooking.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CinemaRepository extends JpaRepository<Cinema, Long> {
    List<Cinema> findByCityId(Long cityId);
    
    @Query("SELECT c FROM Cinema c JOIN FETCH c.theaterChain WHERE c.city.id = :cityId")
    List<Cinema> findByCityIdWithTheaterChain(@Param("cityId") Long cityId);
    
    List<Cinema> findByTheaterChainId(Long theaterChainId);
}
