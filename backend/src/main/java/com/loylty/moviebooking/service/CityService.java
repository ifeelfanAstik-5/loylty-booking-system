package com.loylty.moviebooking.service;

import com.loylty.moviebooking.dto.CityDto;
import com.loylty.moviebooking.dto.ShowDto;
import com.loylty.moviebooking.dto.CinemaDto;
import com.loylty.moviebooking.dto.MovieDto;
import com.loylty.moviebooking.entity.City;
import com.loylty.moviebooking.entity.Show;
import com.loylty.moviebooking.repository.CityRepository;
import com.loylty.moviebooking.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityService {
    
    private final CityRepository cityRepository;
    private final ShowRepository showRepository;
    
    public List<CityDto> getAllCities() {
        return cityRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public CityDto getCityById(Long id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("City not found with id: " + id));
        return convertToDto(city);
    }
    
    public List<ShowDto> getShowsByCity(Long cityId) {
        return showRepository.findShowsByCity(cityId).stream()
                .map(this::convertToShowDto)
                .collect(Collectors.toList());
    }
    
    private CityDto convertToDto(City city) {
        return new CityDto(city.getId(), city.getName());
    }
    
    private ShowDto convertToShowDto(Show show) {
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
