package com.loylty.moviebooking.service;

import com.loylty.moviebooking.dto.CityDto;
import com.loylty.moviebooking.entity.City;
import com.loylty.moviebooking.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityService {
    
    private final CityRepository cityRepository;
    
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
    
    private CityDto convertToDto(City city) {
        return new CityDto(city.getId(), city.getName());
    }
}
