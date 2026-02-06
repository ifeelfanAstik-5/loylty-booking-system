package com.loylty.moviebooking.service;

import com.loylty.moviebooking.cache.SeatLockService;
import com.loylty.moviebooking.dto.*;
import com.loylty.moviebooking.entity.*;
import com.loylty.moviebooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {
    
    private final SeatLockService seatLockService;
    private final ShowRepository showRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatRepository seatRepository;
    
    public SeatLockResponse lockSeats(SeatLockRequest request) {
        // Generate a user ID if not provided
        String userId = request.getUserId() != null ? request.getUserId() : UUID.randomUUID().toString();
        
        // Validate show exists
        showRepository.findById(request.getShowId())
                .orElseThrow(() -> new RuntimeException("Show not found with id: " + request.getShowId()));
        
        // Try to lock seats
        boolean locked = seatLockService.lockSeats(request.getShowId(), request.getSeatIds(), userId, 5);
        
        if (locked) {
            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);
            return new SeatLockResponse(
                    true,
                    "Seats locked successfully",
                    request.getSeatIds(),
                    expiryTime,
                    userId
            );
        } else {
            return new SeatLockResponse(
                    false,
                    "Some seats are already locked or booked",
                    null,
                    null,
                    null
            );
        }
    }
    
    public SeatLockResponse unlockSeats(SeatLockRequest request) {
        if (request.getUserId() == null) {
            return new SeatLockResponse(
                    false,
                    "User ID is required to unlock seats",
                    null,
                    null,
                    null
            );
        }
        
        boolean unlocked = seatLockService.unlockSeats(request.getShowId(), request.getSeatIds(), request.getUserId());
        
        return new SeatLockResponse(
                unlocked,
                unlocked ? "Seats unlocked successfully" : "Failed to unlock seats",
                unlocked ? request.getSeatIds() : null,
                null,
                request.getUserId()
        );
    }
    
    public boolean confirmBooking(Long showId, java.util.List<Long> seatIds, String userId) {
        return seatLockService.confirmBooking(showId, seatIds, userId);
    }
    
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        // Validate show exists
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new RuntimeException("Show not found with id: " + request.getShowId()));
        
        // Confirm booking (this will validate seat locks)
        boolean confirmed = seatLockService.confirmBooking(request.getShowId(), request.getSeatIds(), request.getUserId());
        if (!confirmed) {
            throw new RuntimeException("Failed to confirm booking. Seats may no longer be locked.");
        }
        
        // Get seats and calculate total amount
        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());
        BigDecimal totalAmount = calculateTotalAmount(seats, show);
        
        // Create booking
        Booking booking = new Booking();
        booking.setShow(show);
        booking.setGuestName(request.getGuestName());
        booking.setGuestEmail(request.getGuestEmail());
        booking.setTotalAmount(totalAmount);
        booking.setStatus("CONFIRMED");
        
        booking = bookingRepository.save(booking);
        
        // Create booking seats
        for (Seat seat : seats) {
            BookingSeat bookingSeat = new BookingSeat();
            bookingSeat.setBooking(booking);
            bookingSeat.setSeat(seat);
            bookingSeat.setPrice(getSeatPrice(seat, show));
            bookingSeatRepository.save(bookingSeat);
        }
        
        return convertToResponse(booking, seats);
    }
    
    private BigDecimal calculateTotalAmount(List<Seat> seats, Show show) {
        return seats.stream()
                .map(seat -> getSeatPrice(seat, show))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal getSeatPrice(Seat seat, Show show) {
        return seat.getCategory() == Seat.SeatCategory.PREMIUM ? 
                show.getPremiumPrice() : show.getBasePrice();
    }
    
    private BookingResponse convertToResponse(Booking booking, List<Seat> seats) {
        List<SeatDto> seatDtos = seats.stream()
                .map(seat -> new SeatDto(seat.getId(), seat.getRowNumber(), seat.getSeatNumber(), 
                        seat.getCategory().name(), "BOOKED", null, null))
                .collect(Collectors.toList());
        
        return new BookingResponse(
                booking.getId(),
                booking.getGuestName(),
                booking.getGuestEmail(),
                booking.getShow().getMovie().getTitle(),
                booking.getShow().getScreen().getCinema().getName(),
                booking.getShow().getScreen().getName(),
                booking.getShow().getShowTime(),
                seatDtos,
                booking.getTotalAmount(),
                booking.getBookingTime(),
                booking.getStatus()
        );
    }
}
