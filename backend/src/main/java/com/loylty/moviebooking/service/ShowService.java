package com.loylty.moviebooking.service;

import com.loylty.moviebooking.dto.ShowDto;
import com.loylty.moviebooking.dto.CinemaDto;
import com.loylty.moviebooking.dto.MovieDto;
import com.loylty.moviebooking.dto.SeatDto;
import com.loylty.moviebooking.entity.Show;
import com.loylty.moviebooking.entity.ShowSeat;
import com.loylty.moviebooking.repository.ShowRepository;
import com.loylty.moviebooking.repository.BookingSeatRepository;
import com.loylty.moviebooking.cache.SeatLockService;
import com.loylty.moviebooking.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowService {
    
    private final ShowRepository showRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatLockService seatLockService;
    private final ShowSeatRepository showSeatRepository;
    
    public List<ShowDto> getShowsByMovieAndCity(Long movieId, Long cityId) {
        return showRepository.findShowsByMovieAndCity(movieId, cityId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public Map<String, List<ShowDto>> getShowsByMovieAndCityGroupedByCinema(Long movieId, Long cityId) {
        List<ShowDto> shows = getShowsByMovieAndCity(movieId, cityId);
        
        return shows.stream()
                .collect(Collectors.groupingBy(
                        show -> {
                            CinemaDto cinema = show.getCinema();
                            return String.format("{\"id\":%d,\"name\":\"%s\",\"address\":\"%s\",\"theaterChainName\":\"%s\"}",
                                    cinema.getId(), cinema.getName(), cinema.getAddress(), cinema.getTheaterChainName());
                        },
                        Collectors.toList()
                ));
    }
    
    public ShowDto getShowById(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Show not found with id: " + id));
        return convertToDto(show);
    }
    
    public List<CinemaDto> getCinemasWithShowtimes(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found with id: " + showId));
        
        CinemaDto cinemaDto = new CinemaDto(
                show.getScreen().getCinema().getId(),
                show.getScreen().getCinema().getName(),
                show.getScreen().getCinema().getAddress(),
                show.getScreen().getCinema().getTheaterChain().getName()
        );
        
        return List.of(cinemaDto);
    }
    
    public List<SeatDto> getSeatLayoutAndAvailability(Long showId) {
        // Use the new ShowSeat system
        List<ShowSeat> showSeats = showSeatRepository.findByShowIdOrderByRowNumberAscSeatNumberAsc(showId);
        
        return showSeats.stream()
                .map(this::convertToShowSeatDto)
                .collect(Collectors.toList());
    }
    
    private SeatDto convertToShowSeatDto(ShowSeat showSeat) {
        return new SeatDto(
                showSeat.getId(),
                showSeat.getRowNumber(),
                showSeat.getSeatNumber(),
                showSeat.getCategory().name(),
                showSeat.getStatus().name(),
                showSeat.getLockUserId(),
                showSeat.getLockExpiryTime()
        );
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
