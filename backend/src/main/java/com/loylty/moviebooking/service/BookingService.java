package com.loylty.moviebooking.service;

import com.loylty.moviebooking.dto.*;
import com.loylty.moviebooking.entity.*;
import com.loylty.moviebooking.repository.*;
import com.loylty.moviebooking.cache.SeatLockService;
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
    private final SeatLockService seatLockService;
    
    public SeatLockResponse lockSeats(SeatLockRequest request) {
        // Generate a user ID if not provided
        String userId = request.getUserId() != null ? request.getUserId() : UUID.randomUUID().toString();
        
        // Validate show exists
        showRepository.findById(request.getShowId())
                .orElseThrow(() -> new RuntimeException("Show not found with id: " + request.getShowId()));
        
        // Use pure in-memory seat locking
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
        
        // Unlock seats in in-memory system
        seatLockService.unlockSeats(request.getShowId(), request.getSeatIds(), request.getUserId());
        
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
            System.out.println("=== DEBUG: confirmBooking called ===");
            System.out.println("showId: " + showId);
            System.out.println("seatIds: " + seatIds);
            System.out.println("userId: " + userId);
            
            // Confirm booking in in-memory system
            boolean confirmed = seatLockService.confirmBooking(showId, seatIds, userId);
            System.out.println("In-memory confirmation: " + confirmed);
            
            if (confirmed) {
                // Create booking record in database
                Show show = showRepository.findById(showId)
                        .orElseThrow(() -> new RuntimeException("Show not found: " + showId));
                
                System.out.println("Show found: " + show.getId());
                
                // Calculate total price
                BigDecimal totalPrice = showSeatService.calculateTotalPrice(showId, seatIds);
                System.out.println("Total price: " + totalPrice);
                
                // Create booking
                Booking booking = new Booking();
                booking.setShow(show);
                booking.setGuestName("Guest"); // Default name
                booking.setGuestEmail("guest@example.com"); // Default email
                booking.setTotalAmount(totalPrice);
                booking.setBookingTime(LocalDateTime.now());
                booking.setStatus("CONFIRMED");
                
                booking = bookingRepository.save(booking);
                System.out.println("Booking saved: " + booking.getId());
                final Booking finalBooking = booking;
                
                // Create booking seats
                BigDecimal seatPrice = totalPrice.divide(BigDecimal.valueOf(seatIds.size()));
                List<BookingSeat> bookingSeats = seatIds.stream()
                        .map(seatId -> {
                            BookingSeat bookingSeat = new BookingSeat();
                            bookingSeat.setBooking(finalBooking);
                            bookingSeat.setSeatId(seatId);
                            bookingSeat.setPrice(seatPrice);
                            return bookingSeat;
                        })
                        .collect(Collectors.toList());
                
                bookingSeatRepository.saveAll(bookingSeats);
                System.out.println("Booking seats saved: " + bookingSeats.size());
            }
            
            return confirmed;
        } catch (Exception e) {
            System.out.println("ERROR in confirmBooking: " + e.getMessage());
            e.printStackTrace();
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
        
        // Calculate total amount
        BigDecimal totalAmount = showSeatService.calculateTotalPrice(request.getShowId(), request.getSeatIds());
        
        // Create booking
        Booking booking = new Booking();
        booking.setShow(show);
        booking.setGuestName(request.getGuestName());
        booking.setGuestEmail(request.getGuestEmail());
        booking.setTotalAmount(totalAmount);
        booking.setStatus("CONFIRMED");
        
        booking = bookingRepository.save(booking);
        
        // Create booking seats
        BigDecimal seatPrice = totalAmount.divide(BigDecimal.valueOf(request.getSeatIds().size()));
        for (Long seatId : request.getSeatIds()) {
            BookingSeat bookingSeat = new BookingSeat();
            bookingSeat.setBooking(booking);
            bookingSeat.setSeatId(seatId);
            bookingSeat.setPrice(seatPrice);
            bookingSeatRepository.save(bookingSeat);
        }
        
        return convertToResponse(booking, request.getSeatIds());
    }
    
    private BookingResponse convertToResponse(Booking booking, List<Long> seatIds) {
        // Convert seat IDs to SeatDto objects
        List<SeatDto> seatDtos = seatIds.stream()
                .map(seatId -> new SeatDto(seatId, 0, 0, "REGULAR", "BOOKED", null, null))
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
