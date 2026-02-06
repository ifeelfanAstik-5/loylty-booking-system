package com.loylty.moviebooking.controller;

import com.loylty.moviebooking.dto.*;
import com.loylty.moviebooking.entity.*;
import com.loylty.moviebooking.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = {"https://loylty-booking-ui.vercel.app", "https://*.vercel.app", "http://localhost:5173"})
public class AdminController {
    
    private final CityService cityService;
    private final MovieService movieService;
    private final ShowService showService;
    
    // City Management
    @PostMapping("/cities")
    public ResponseEntity<CityDto> createCity(@Valid @RequestBody CityDto cityDto) {
        // Implementation would go here
        return ResponseEntity.ok(cityDto);
    }
    
    // Theater Chain Management
    @PostMapping("/theater-chains")
    public ResponseEntity<String> createTheaterChain(@RequestBody TheaterChain theaterChain) {
        // Implementation would go here
        return ResponseEntity.ok("Theater chain created successfully");
    }
    
    // Cinema Management
    @PostMapping("/cinemas")
    public ResponseEntity<String> createCinema(@RequestBody Cinema cinema) {
        // Implementation would go here
        return ResponseEntity.ok("Cinema created successfully");
    }
    
    // Movie Management
    @PostMapping("/movies")
    public ResponseEntity<MovieDto> createMovie(@Valid @RequestBody MovieDto movieDto) {
        // Implementation would go here
        return ResponseEntity.ok(movieDto);
    }
    
    // Show Management
    @PostMapping("/shows")
    public ResponseEntity<ShowDto> createShow(@Valid @RequestBody ShowDto showDto) {
        // Implementation would go here
        return ResponseEntity.ok(showDto);
    }
    
    // Get all cities for admin
    @GetMapping("/cities")
    public ResponseEntity<List<CityDto>> getAllCitiesForAdmin() {
        return ResponseEntity.ok(cityService.getAllCities());
    }
    
    // Get all movies for admin
    @GetMapping("/movies")
    public ResponseEntity<List<MovieDto>> getAllMoviesForAdmin() {
        return ResponseEntity.ok(movieService.searchMovies(""));
    }
    
    // Get all shows for admin
    @GetMapping("/shows")
    public ResponseEntity<List<ShowDto>> getAllShowsForAdmin() {
        // This would need to be implemented in ShowService
        return ResponseEntity.ok(List.of());
    }
}
