package com.loylty.moviebooking.service;

import com.loylty.moviebooking.dto.ShowDto;
import com.loylty.moviebooking.dto.CinemaDto;
import com.loylty.moviebooking.dto.MovieDto;
import com.loylty.moviebooking.entity.Show;
import com.loylty.moviebooking.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowService {
    
    private final ShowRepository showRepository;
    
    public List<ShowDto> getShowsByMovieAndCity(Long movieId, Long cityId) {
        return showRepository.findShowsByMovieAndCity(movieId, cityId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public Map<CinemaDto, List<ShowDto>> getShowsByMovieAndCityGroupedByCinema(Long movieId, Long cityId) {
        List<ShowDto> shows = getShowsByMovieAndCity(movieId, cityId);
        
        return shows.stream()
                .collect(Collectors.groupingBy(
                        ShowDto::getCinema,
                        Collectors.toList()
                ));
    }
    
    public ShowDto getShowById(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Show not found with id: " + id));
        return convertToDto(show);
    }
    
    private ShowDto convertToDto(Show show) {
        MovieDto movieDto = new MovieDto(
                show.getMovie().getId(),
                show.getMovie().getTitle(),
                show.getMovie().getDescription(),
                show.getMovie().getDurationMinutes(),
                show.getMovie().getLanguage(),
                show.getMovie().getGenre(),
                show.getMovie().getRating(),
                show.getMovie().getReleaseDate()
        );
        
        CinemaDto cinemaDto = new CinemaDto(
                show.getScreen().getCinema().getId(),
                show.getScreen().getCinema().getName(),
                show.getScreen().getCinema().getAddress(),
                show.getScreen().getCinema().getTheaterChain().getName()
        );
        
        return new ShowDto(
                show.getId(),
                movieDto,
                cinemaDto,
                show.getScreen().getName(),
                show.getShowTime(),
                show.getEndTime(),
                show.getBasePrice(),
                show.getPremiumPrice()
        );
    }
}
