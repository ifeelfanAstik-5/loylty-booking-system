package com.loylty.moviebooking.service;

import com.loylty.moviebooking.dto.ShowDto;
import com.loylty.moviebooking.dto.CinemaDto;
import com.loylty.moviebooking.dto.MovieDto;
import com.loylty.moviebooking.dto.SeatDto;
import com.loylty.moviebooking.entity.Show;
import com.loylty.moviebooking.entity.Seat;
import com.loylty.moviebooking.repository.ShowRepository;
import com.loylty.moviebooking.repository.SeatRepository;
import com.loylty.moviebooking.repository.BookingSeatRepository;
import com.loylty.moviebooking.cache.SeatLockService;
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
    private final SeatRepository seatRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatLockService seatLockService;
    
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
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found with id: " + showId));
        
        List<Seat> seats = seatRepository.findByScreenIdOrdered(show.getScreen().getId());
        Set<Long> bookedSeatIds = bookingSeatRepository.findBookedSeatIdsByShowId(showId);
        Set<Long> lockedSeatIds = seatLockService.getLockedSeats(showId);
        
        return seats.stream()
                .map(seat -> convertToSeatDto(seat, bookedSeatIds, lockedSeatIds, showId))
                .collect(Collectors.toList());
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
    
    private SeatDto convertToSeatDto(Seat seat, Set<Long> bookedSeatIds, Set<Long> lockedSeatIds, Long showId) {
        String status;
        String lockUserId = null;
        java.time.LocalDateTime lockExpiryTime = null;
        
        if (bookedSeatIds.contains(seat.getId())) {
            status = "BOOKED";
        } else if (lockedSeatIds.contains(seat.getId())) {
            status = "LOCKED";
            SeatLockService.SeatLockInfo lockInfo = seatLockService.getSeatLockInfo(showId, seat.getId());
            if (lockInfo != null) {
                lockUserId = lockInfo.getUserId();
                lockExpiryTime = lockInfo.getExpiryTime();
            }
        } else {
            status = "AVAILABLE";
        }
        
        return new SeatDto(
                seat.getId(),
                seat.getRowNumber(),
                seat.getSeatNumber(),
                seat.getCategory().name(),
                status,
                lockUserId,
                lockExpiryTime
        );
    }
}
