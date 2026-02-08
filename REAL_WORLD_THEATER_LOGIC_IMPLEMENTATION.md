# Real-World Theater Seating Plan Implementation Guide

## 🎯 **Objective**
Implement proper real-world theater logic where shows have complete seating plans that start empty and fill as bookings happen, following the cascade logic: **City → Movies → Shows → Seating Plans**.

## 📋 **Core Business Requirements**

### 1. **Seat Immutability**
- **No seat creation/deletion after show creation**
- Seats can only be: **BOOKED** or **UNBOOKED**
- No dynamic seat management during runtime

### 2. **Cascade Logic**
- **City** may or may not have **Movies**
- If City has **Movies**, it must have at least one **Show**
- If City has **Shows**, each show must have a **complete seating plan**
- **Fixed number of seats** assigned at show creation (automatic or manual)

### 3. **Real-World Theater Logic**
- Shows start with **empty seating plans**
- Seats fill up as **bookings happen**
- **ShowSeatState** tracks individual seat status per show
- **No seat creation/deletion via API**

---

## 🏗️ **Implementation Plan**

### **Phase 1: Database Schema Changes**

#### 1.1 **New Entity: ShowSeatState**
```java
@Entity
@Table(name = "show_seat_states")
public class ShowSeatState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;
    
    @Column(name = "lock_user_id")
    private String lockUserId;
    
    @Column(name = "lock_expiry_time")
    private LocalDateTime lockExpiryTime;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum SeatStatus {
        AVAILABLE, LOCKED, BOOKED
    }
}
```

#### 1.2 **Repository: ShowSeatStateRepository**
```java
@Repository
public interface ShowSeatStateRepository extends JpaRepository<ShowSeatState, Long> {
    
    List<ShowSeatState> findByShowId(Long showId);
    
    List<ShowSeatState> findByShowIdAndSeatIdIn(Long showId, List<Long> seatIds);
    
    List<ShowSeatState> findByShowIdAndStatus(Long showId, ShowSeatState.SeatStatus status);
    
    List<ShowSeatState> findByShowIdAndLockUserIdAndLockExpiryTimeAfter(
        Long showId, String userId, LocalDateTime now);
    
    void deleteByShowIdAndLockExpiryTimeBefore(LocalDateTime now);
    
    @Query("SELECT s FROM ShowSeatState s WHERE s.show.id = :showId AND s.seat.id = :seatId")
    Optional<ShowSeatState> findByShowIdAndSeatId(@Param("showId") Long showId, @Param("seatId") Long seatId);
}
```

#### 1.3 **Database Migration Script**
```sql
-- Create show_seat_states table
CREATE TABLE show_seat_states (
    id BIGSERIAL PRIMARY KEY,
    show_id BIGINT NOT NULL REFERENCES shows(id),
    seat_id BIGINT NOT NULL REFERENCES seats(id),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    lock_user_id VARCHAR(255),
    lock_expiry_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(show_id, seat_id)
);

-- Create indexes for performance
CREATE INDEX idx_show_seat_states_show_id ON show_seat_states(show_id);
CREATE INDEX idx_show_seat_states_status ON show_seat_states(status);
CREATE INDEX idx_show_seat_states_lock_expiry ON show_seat_states(lock_expiry_time);

-- Seed data for existing shows
INSERT INTO show_seat_states (show_id, seat_id, status, created_at, updated_at)
SELECT 
    s.id as show_id,
    st.id as seat_id,
    'AVAILABLE' as status,
    NOW() as created_at,
    NOW() as updated_at
FROM shows s
JOIN screens sc ON s.screen_id = sc.id
JOIN seats st ON st.screen_id = sc.id
WHERE s.id IN (SELECT id FROM shows);
```

---

### **Phase 2: Service Layer Implementation**

#### 2.1 **ShowSeatStateService**
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ShowSeatStateService {
    
    private final ShowSeatStateRepository showSeatStateRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    
    /**
     * Initialize seating plan for a new show
     */
    @Transactional
    public void initializeSeatingPlan(Long showId) {
        Show show = showRepository.findById(showId)
            .orElseThrow(() -> new EntityNotFoundException("Show not found: " + showId));
        
        List<Seat> seats = seatRepository.findByScreenId(show.getScreen().getId());
        
        List<ShowSeatState> seatStates = seats.stream()
            .map(seat -> ShowSeatState.builder()
                .show(show)
                .seat(seat)
                .status(ShowSeatState.SeatStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build())
            .collect(Collectors.toList());
        
        showSeatStateRepository.saveAll(seatStates);
        log.info("Initialized seating plan for show {}: {} seats", showId, seatStates.size());
    }
    
    /**
     * Get seat layout with availability for a show
     */
    public List<SeatDto> getSeatLayout(Long showId) {
        List<ShowSeatState> seatStates = showSeatStateRepository.findByShowId(showId);
        
        return seatStates.stream()
            .map(this::convertToSeatDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Lock seats for booking
     */
    @Transactional
    public boolean lockSeats(Long showId, List<Long> seatIds, String userId, int lockDurationMinutes) {
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(lockDurationMinutes);
        
        List<ShowSeatState> seatStates = showSeatStateRepository.findByShowIdAndSeatIdIn(showId, seatIds);
        
        // Check if all seats are available
        boolean allAvailable = seatStates.stream()
            .allMatch(state -> state.getStatus() == ShowSeatState.SeatStatus.AVAILABLE);
        
        if (!allAvailable) {
            return false;
        }
        
        // Lock seats
        seatStates.forEach(state -> {
            state.setStatus(ShowSeatState.SeatStatus.LOCKED);
            state.setLockUserId(userId);
            state.setLockExpiryTime(expiryTime);
            state.setUpdatedAt(LocalDateTime.now());
        });
        
        showSeatStateRepository.saveAll(seatStates);
        return true;
    }
    
    /**
     * Book seats (update status from LOCKED to BOOKED)
     */
    @Transactional
    public void bookSeats(Long showId, List<Long> seatIds) {
        List<ShowSeatState> seatStates = showSeatStateRepository.findByShowIdAndSeatIdIn(showId, seatIds);
        
        seatStates.forEach(state -> {
            if (state.getStatus() == ShowSeatState.SeatStatus.LOCKED) {
                state.setStatus(ShowSeatState.SeatStatus.BOOKED);
                state.setLockUserId(null);
                state.setLockExpiryTime(null);
                state.setUpdatedAt(LocalDateTime.now());
            }
        });
        
        showSeatStateRepository.saveAll(seatStates);
    }
    
    /**
     * Release expired locks
     */
    @Transactional
    public void releaseExpiredLocks() {
        showSeatStateRepository.deleteByShowIdAndLockExpiryTimeBefore(LocalDateTime.now());
    }
    
    private SeatDto convertToSeatDto(ShowSeatState state) {
        return SeatDto.builder()
            .id(state.getSeat().getId())
            .rowNumber(state.getSeat().getRowNumber())
            .seatNumber(state.getSeat().getSeatNumber())
            .category(state.getSeat().getCategory().name())
            .status(state.getStatus().name())
            .lockUserId(state.getLockUserId())
            .lockExpiryTime(state.getLockExpiryTime())
            .build();
    }
}
```

#### 2.2 **Updated ShowService**
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ShowService {
    
    private final ShowRepository showRepository;
    private final ShowSeatStateService showSeatStateService;
    
    /**
     * Create new show with automatic seating plan initialization
     */
    @Transactional
    public ShowDto createShow(CreateShowRequest request) {
        // Create show
        Show show = Show.builder()
            .movie(movieRepository.findById(request.getMovieId()).orElseThrow())
            .screen(screenRepository.findById(request.getScreenId()).orElseThrow())
            .showTime(request.getShowTime())
            .endTime(request.getEndTime())
            .basePrice(request.getBasePrice())
            .premiumPrice(request.getPremiumPrice())
            .build();
        
        show = showRepository.save(show);
        
        // Initialize seating plan
        showSeatStateService.initializeSeatingPlan(show.getId());
        
        return convertToDto(show);
    }
    
    /**
     * Get shows with seating plan availability
     */
    public List<ShowDto> getShowsByMovieAndCity(Long movieId, Long cityId) {
        List<Show> shows = showRepository.findShowsByMovieAndCity(movieId, cityId);
        
        return shows.stream()
            .filter(show -> hasValidSeatingPlan(show.getId()))
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    private boolean hasValidSeatingPlan(Long showId) {
        try {
            List<SeatDto> seatLayout = showSeatStateService.getSeatLayout(showId);
            return !seatLayout.isEmpty();
        } catch (Exception e) {
            log.error("Error checking seating plan for show {}: {}", showId, e.getMessage());
            return false;
        }
    }
}
```

---

### **Phase 3: Repository Layer Changes**

#### 3.1 **Updated SeatRepository**
```java
@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    // Read-only operations (no save/delete)
    List<Seat> findByScreenId(Long screenId);
    
    @Query("SELECT s FROM Seat s WHERE s.screen.id = :screenId ORDER BY s.rowNumber, s.seatNumber")
    List<Seat> findByScreenIdOrderByRowNumberSeatNumber(@Param("screenId") Long screenId);
    
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.screen.id = :screenId")
    int countByScreenId(@Param("screenId") Long screenId);
    
    // No save/delete methods - seats are immutable after creation
}
```

#### 3.2 **New SeatAdminRepository**
```java
@Repository
public interface SeatAdminRepository extends JpaRepository<Seat, Long> {
    
    // Admin-only operations for seat management
    List<Seat> findByScreenId(Long screenId);
    
    @Query("SELECT s FROM Seat s WHERE s.screen.id = :screenId ORDER BY s.rowNumber, s.seatNumber")
    List<Seat> findByScreenIdOrderByRowNumberSeatNumber(@Param("screenId") Long screenId);
}
```

---

### **Phase 4: API Layer Changes**

#### 4.1 **Updated SeatController**
```java
@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {
    
    private final ShowSeatStateService showSeatStateService;
    
    /**
     * Get seat layout for a show
     */
    @GetMapping("/shows/{showId}/layout")
    public ResponseEntity<List<SeatDto>> getSeatLayout(@PathVariable Long showId) {
        List<SeatDto> seatLayout = showSeatStateService.getSeatLayout(showId);
        return ResponseEntity.ok(seatLayout);
    }
    
    /**
     * Get available seats for a show
     */
    @GetMapping("/shows/{showId}/available")
    public ResponseEntity<List<SeatDto>> getAvailableSeats(@PathVariable Long showId) {
        List<ShowSeatState> availableStates = showSeatStateRepository
            .findByShowIdAndStatus(showId, ShowSeatState.SeatStatus.AVAILABLE);
        
        List<SeatDto> availableSeats = availableStates.stream()
            .map(this::convertToSeatDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(availableSeats);
    }
    
    // NO create/delete endpoints - seats are immutable
}
```

#### 4.2 **Updated BookingService**
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    
    private final ShowSeatStateService showSeatStateService;
    
    /**
     * Create booking with seat state management
     */
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        // Lock seats first
        boolean locked = showSeatStateService.lockSeats(
            request.getShowId(), 
            request.getSeatIds(), 
            request.getUserId(), 
            15 // 15 minutes lock
        );
        
        if (!locked) {
            throw new SeatNotAvailableException("Seats are not available for booking");
        }
        
        // Create booking
        Booking booking = createBookingEntity(request);
        booking = bookingRepository.save(booking);
        
        // Update seat states to BOOKED
        showSeatStateService.bookSeats(request.getShowId(), request.getSeatIds());
        
        return convertToResponse(booking);
    }
}
```

---

### **Phase 5: Data Validation & Business Logic**

#### 5.1 **DataIntegrityValidationService**
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class DataIntegrityValidationService {
    
    /**
     * Validate city → movies → shows → seating plan cascade
     */
    public ValidationResult validateCityDataIntegrity(Long cityId) {
        ValidationResult result = new ValidationResult();
        
        City city = cityRepository.findById(cityId)
            .orElseThrow(() -> new EntityNotFoundException("City not found: " + cityId));
        
        // Check if city has movies
        List<Movie> movies = movieRepository.findByCityId(cityId);
        
        if (!movies.isEmpty()) {
            // Each movie must have at least one show
            for (Movie movie : movies) {
                List<Show> shows = showRepository.findByMovieId(movie.getId());
                
                if (shows.isEmpty()) {
                    result.addError("Movie '" + movie.getTitle() + "' has no shows");
                } else {
                    // Each show must have valid seating plan
                    for (Show show : shows) {
                        validateShowSeatingPlan(show, result);
                    }
                }
            }
        }
        
        return result;
    }
    
    private void validateShowSeatingPlan(Show show, ValidationResult result) {
        try {
            List<ShowSeatState> seatStates = showSeatStateRepository.findByShowId(show.getId());
            
            if (seatStates.isEmpty()) {
                result.addError("Show " + show.getId() + " has no seating plan");
                return;
            }
            
            // Check if all seats from screen are present
            List<Seat> screenSeats = seatRepository.findByScreenId(show.getScreen().getId());
            
            if (seatStates.size() != screenSeats.size()) {
                result.addError("Show " + show.getId() + " has incomplete seating plan");
            }
            
        } catch (Exception e) {
            result.addError("Error validating seating plan for show " + show.getId() + ": " + e.getMessage());
        }
    }
    
    public static class ValidationResult {
        private List<String> errors = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return errors;
        }
    }
}
```

---

### **Phase 6: Frontend Integration**

#### 6.1 **Seat Selection Component Updates**
```javascript
// Updated seat selection to use ShowSeatState API
const fetchSeatLayout = async (showId) => {
    try {
        const response = await api.get(`/api/seats/shows/${showId}/layout`);
        return response.data;
    } catch (error) {
        console.error('Error fetching seat layout:', error);
        return [];
    }
};

const lockSeats = async (showId, seatIds, userId) => {
    try {
        const response = await api.post(`/api/bookings/lock`, {
            showId,
            seatIds,
            userId
        });
        return response.data.success;
    } catch (error) {
        console.error('Error locking seats:', error);
        return false;
    }
};
```

---

## 🚀 **Deployment Steps**

### **Step 1: Database Migration**
1. Run the SQL migration script to create `show_seat_states` table
2. Seed existing shows with seating plans
3. Create indexes for performance

### **Step 2: Backend Deployment**
1. Deploy new entities and repositories
2. Update service layer with ShowSeatState logic
3. Update API endpoints
4. Add data validation

### **Step 3: Testing & Validation**
1. Test seat immutability (no create/delete)
2. Test seating plan initialization
3. Test seat locking and booking
4. Test cascade validation

### **Step 4: Frontend Updates**
1. Update seat selection to use new API
2. Add seat status indicators
3. Update booking flow

---

## 📊 **Performance Considerations**

### **Database Optimization**
- Indexes on `show_id`, `status`, `lock_expiry_time`
- Batch operations for seat state updates
- Connection pooling for concurrent bookings

### **Caching Strategy**
- Cache seat layouts for frequently accessed shows
- Cache available seats count
- Invalidate cache on booking/lock operations

### **Concurrency Handling**
- Pessimistic locking for seat allocation
- Optimistic locking for seat state updates
- Distributed locks for multi-instance deployment

---

## 🔒 **Security Considerations**

### **Seat Lock Security**
- Time-based locks with expiry
- User-specific lock ownership
- Automatic cleanup of expired locks

### **API Security**
- Rate limiting on seat lock endpoints
- Authentication for booking operations
- Audit logging for seat state changes

---

## 📈 **Monitoring & Analytics**

### **Key Metrics**
- Seat utilization rate per show
- Booking conversion rate
- Lock expiry rate
- Concurrent booking attempts

### **Health Checks**
- Database connectivity
- Seat state consistency
- Lock cleanup process status

---

## 🎯 **Success Criteria**

### **Functional Requirements**
✅ Seats are immutable after show creation  
✅ Complete seating plans for all shows  
✅ Real-time seat status tracking  
✅ Proper cascade validation  
✅ No seat creation/deletion via API  

### **Performance Requirements**
✅ < 100ms response time for seat layout  
✅ Handle 1000+ concurrent seat selections  
✅ 99.9% uptime during peak hours  

### **Business Requirements**
✅ Real-world theater behavior  
✅ Proper seat state management  
✅ Data integrity enforcement  
✅ Scalable architecture  

---

## 🔄 **Rollback Plan**

### **Database Rollback**
```sql
-- Drop show_seat_states table
DROP TABLE IF EXISTS show_seat_states;

-- This is safe as it's a new table, no existing data affected
```

### **Code Rollback**
- Revert to previous entity structure
- Remove ShowSeatState dependencies
- Restore original booking logic

---

## 📝 **Notes & Considerations**

1. **Backward Compatibility**: New system is compatible with existing data
2. **Data Migration**: Existing shows get seating plans automatically
3. **Performance**: Optimized for high-concurrency booking scenarios
4. **Scalability**: Designed for horizontal scaling
5. **Testing**: Comprehensive unit and integration tests required

This implementation provides a robust, real-world theater seating system that enforces proper business logic while maintaining data integrity and performance.
