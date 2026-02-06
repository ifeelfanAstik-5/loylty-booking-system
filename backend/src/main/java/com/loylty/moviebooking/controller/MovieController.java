package com.loylty.moviebooking.controller;

import com.loylty.moviebooking.dto.MovieDto;
import com.loylty.moviebooking.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class MovieController {
    
    private final MovieService movieService;
    
    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<MovieDto>> getMoviesByCity(@PathVariable Long cityId) {
        return ResponseEntity.ok(movieService.getMoviesByCity(cityId));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<MovieDto>> searchMovies(@RequestParam String title) {
        return ResponseEntity.ok(movieService.searchMovies(title));
    }
}
