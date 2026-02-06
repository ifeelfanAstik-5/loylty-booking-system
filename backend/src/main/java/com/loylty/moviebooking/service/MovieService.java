package com.loylty.moviebooking.service;

import com.loylty.moviebooking.dto.MovieDto;
import com.loylty.moviebooking.entity.Movie;
import com.loylty.moviebooking.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {
    
    private final MovieRepository movieRepository;
    
    public List<MovieDto> getMoviesByCity(Long cityId) {
        return movieRepository.findMoviesByCity(cityId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<MovieDto> searchMovies(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private MovieDto convertToDto(Movie movie) {
        return new MovieDto(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getDurationMinutes(),
                movie.getLanguage(),
                movie.getGenre(),
                movie.getRating(),
                movie.getReleaseDate()
        );
    }
}
