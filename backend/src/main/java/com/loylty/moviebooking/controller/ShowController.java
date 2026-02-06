package com.loylty.moviebooking.controller;

import com.loylty.moviebooking.dto.ShowDto;
import com.loylty.moviebooking.service.ShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shows")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ShowController {
    
    private final ShowService showService;
    
    @GetMapping("/movie/{movieId}/city/{cityId}")
    public ResponseEntity<List<ShowDto>> getShowsByMovieAndCity(
            @PathVariable("movieId") Long movieId,
            @PathVariable("cityId") Long cityId) {
        return ResponseEntity.ok(showService.getShowsByMovieAndCity(movieId, cityId));
    }
    
    @GetMapping("/movie/{movieId}/city/{cityId}/grouped")
    public ResponseEntity<Map<com.loylty.moviebooking.dto.CinemaDto, List<ShowDto>>> getShowsByMovieAndCityGroupedByCinema(
            @PathVariable("movieId") Long movieId,
            @PathVariable("cityId") Long cityId) {
        return ResponseEntity.ok(showService.getShowsByMovieAndCityGroupedByCinema(movieId, cityId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ShowDto> getShowById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(showService.getShowById(id));
    }
}
