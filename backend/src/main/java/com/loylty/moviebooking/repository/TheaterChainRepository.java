package com.loylty.moviebooking.repository;

import com.loylty.moviebooking.entity.TheaterChain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TheaterChainRepository extends JpaRepository<TheaterChain, Long> {
    Optional<TheaterChain> findByName(String name);
    boolean existsByName(String name);
}
