package com.loylty.moviebooking.controller;

import com.loylty.moviebooking.dto.CityDto;
import com.loylty.moviebooking.dto.ShowDto;
import com.loylty.moviebooking.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cities")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CityController {
    
    private final CityService cityService;
    
    @GetMapping
    public ResponseEntity<List<CityDto>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CityDto> getCityById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(cityService.getCityById(id));
    }
    
    @GetMapping("/{cityId}/shows")
    public ResponseEntity<List<ShowDto>> getShowsByCity(@PathVariable("cityId") Long cityId) {
        return ResponseEntity.ok(cityService.getShowsByCity(cityId));
    }
}
