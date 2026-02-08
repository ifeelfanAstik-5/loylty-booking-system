package com.loylty.moviebooking.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "show_seats", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"show_id", "row_number", "seat_number"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowSeat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;
    
    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;
    
    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private SeatCategory category = SeatCategory.REGULAR;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal price;
    
    @Column(name = "lock_user_id")
    private String lockUserId;
    
    @Column(name = "lock_expiry_time")
    private LocalDateTime lockExpiryTime;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum SeatCategory {
        REGULAR, PREMIUM, VIP
    }
    
    public enum SeatStatus {
        AVAILABLE, LOCKED, BOOKED
    }
}
