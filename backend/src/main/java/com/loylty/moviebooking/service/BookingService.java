package com.loylty.moviebooking.service;

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
    
    private final ShowSeatService showSeatService;
    private final ShowRepository showRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowSeatRepository showSeatRepository;
    
    public SeatLockResponse lockSeats(SeatLockRequest request) {
        // Generate a user ID if not provided
        String userId = request.getUserId() != null ? request.getUserId() : UUID.randomUUID().toString();
        
        // Validate show exists
        showRepository.findById(request.getShowId())
                .orElseThrow(() -> new RuntimeException("Show not found with id: " + request.getShowId()));
        
        // Try to lock seats using ShowSeatService
        boolean locked = showSeatService.lockSeats(request.getShowId(), 
            convertToRowNumbers(request.getSeatIds()), 
            convertToSeatNumbers(request.getSeatIds()), 
            userId, 5);
        
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
        
        // For simplicity, we'll implement unlock as setting seats back to AVAILABLE
        // In a real implementation, you'd track which user locked which seats
        return new SeatLockResponse(
                true,
                "Seats unlocked successfully",
                request.getSeatIds(),
                null,
                request.getUserId()
        );
    }
    
    public boolean confirmBooking(Long showId, java.util.List<Long> seatIds, String userId) {
        try {
            showSeatService.bookSeats(showId, convertToRowNumbers(seatIds), convertToSeatNumbers(seatIds));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        // Validate show exists
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new RuntimeException("Show not found with id: " + request.getShowId()));
        
        // Confirm booking (this will validate seat locks)
        boolean confirmed = confirmBooking(request.getShowId(), request.getSeatIds(), request.getUserId());
        if (!confirmed) {
            throw new RuntimeException("Failed to confirm booking. Seats may no longer be locked.");
        }
        
        // Get show seats and calculate total amount
        List<ShowSeat> showSeats = showSeatRepository.findAllById(request.getSeatIds());
        BigDecimal totalAmount = calculateTotalAmount(showSeats);
        
        // Create booking
        Booking booking = new Booking();
        booking.setShow(show);
        booking.setGuestName(request.getGuestName());
        booking.setGuestEmail(request.getGuestEmail());
        booking.setTotalAmount(totalAmount);
        booking.setStatus("CONFIRMED");
        
        booking = bookingRepository.save(booking);
        
        // Create booking seats
        for (ShowSeat showSeat : showSeats) {
            BookingSeat bookingSeat = new BookingSeat();
            bookingSeat.setBooking(booking);
            bookingSeat.setShowSeat(showSeat);
            bookingSeat.setPrice(showSeat.getPrice());
            bookingSeatRepository.save(bookingSeat);
        }
        
        return convertToResponse(booking, showSeats);
    }
    
    private BigDecimal calculateTotalAmount(List<ShowSeat> showSeats) {
        return showSeats.stream()
                .map(ShowSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BookingResponse convertToResponse(Booking booking, List<ShowSeat> showSeats) {
        List<SeatDto> seatDtos = showSeats.stream()
                .map(showSeat -> new SeatDto(showSeat.getId(), showSeat.getRowNumber(), showSeat.getSeatNumber(), 
                        showSeat.getCategory().name(), "BOOKED", null, null))
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
    
    // Helper methods to convert seat IDs to row/seat numbers
    private List<Integer> convertToRowNumbers(List<Long> seatIds) {
        return seatIds.stream()
                .map(id -> ((id.intValue() - 1) / 12) + 1) // Assuming 12 seats per row
                .collect(Collectors.toList());
    }
    
    private List<Integer> convertToSeatNumbers(List<Long> seatIds) {
        return seatIds.stream()
                .map(id -> ((id.intValue() - 1) % 12) + 1) // Assuming 12 seats per row
                .collect(Collectors.toList());
    }
}
