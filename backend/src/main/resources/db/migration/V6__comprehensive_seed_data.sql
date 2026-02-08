-- Comprehensive Seed Data Migration
-- This ensures all screens have seating plans and adds shows across all cities

-- Add more shows across different cities and times
INSERT INTO shows (movie_id, screen_id, show_time, end_time, base_price, premium_price, created_at, updated_at) VALUES
-- Mumbai shows
(1, 1, '2024-02-08 10:30:00', '2024-02-08 13:16:00', 280.00, 400.00, NOW(), NOW()),
(2, 1, '2024-02-08 15:00:00', '2024-02-08 17:46:00', 260.00, 380.00, NOW(), NOW()),
(3, 1, '2024-02-08 20:00:00', '2024-02-08 22:38:00', 240.00, 350.00, NOW(), NOW()),
(4, 2, '2024-02-08 11:30:00', '2024-02-08 14:14:00', 300.00, 450.00, NOW(), NOW()),
(5, 2, '2024-02-08 16:30:00', '2024-02-08 19:14:00', 320.00, 480.00, NOW(), NOW()),
(1, 3, '2024-02-08 09:00:00', '2024-02-08 11:46:00', 250.00, 350.00, NOW(), NOW()),
(2, 3, '2024-02-08 14:00:00', '2024-02-08 16:46:00', 220.00, 320.00, NOW(), NOW()),
(3, 3, '2024-02-08 18:30:00', '2024-02-08 21:08:00', 200.00, 300.00, NOW(), NOW()),

-- Delhi shows  
(1, 4, '2024-02-08 11:00:00', '2024-02-08 13:46:00', 290.00, 420.00, NOW(), NOW()),
(2, 4, '2024-02-08 15:30:00', '2024-02-08 18:16:00', 270.00, 390.00, NOW(), NOW()),
(3, 4, '2024-02-08 20:00:00', '2024-02-08 22:38:00', 250.00, 360.00, NOW(), NOW()),
(4, 5, '2024-02-08 10:00:00', '2024-02-08 12:44:00', 310.00, 460.00, NOW(), NOW()),
(5, 5, '2024-02-08 14:30:00', '2024-02-08 17:14:00', 330.00, 490.00, NOW(), NOW()),
(1, 5, '2024-02-08 19:00:00', '2024-02-08 21:46:00', 280.00, 400.00, NOW(), NOW()),

-- Chennai shows
(2, 6, '2024-02-08 09:30:00', '2024-02-08 12:14:00', 230.00, 330.00, NOW(), NOW()),
(3, 6, '2024-02-08 13:30:00', '2024-02-08 16:08:00', 210.00, 310.00, NOW(), NOW()),
(4, 6, '2024-02-08 17:30:00', '2024-02-08 20:14:00', 240.00, 350.00, NOW(), NOW()),
(5, 6, '2024-02-08 21:00:00', '2024-02-08 23:38:00', 260.00, 380.00, NOW(), NOW()),

-- Kolkata shows
(1, 1, '2024-02-08 12:00:00', '2024-02-08 14:46:00', 260.00, 370.00, NOW(), NOW()),
(2, 1, '2024-02-08 16:00:00', '2024-02-08 18:46:00', 240.00, 350.00, NOW(), NOW()),
(3, 1, '2024-02-08 20:30:00', '2024-02-08 23:08:00', 220.00, 320.00, NOW(), NOW()),
(4, 2, '2024-02-08 10:30:00', '2024-02-08 13:14:00', 290.00, 420.00, NOW(), NOW()),
(5, 2, '2024-02-08 15:00:00', '2024-02-08 17:38:00', 310.00, 450.00, NOW(), NOW()),

-- Pune shows
(2, 3, '2024-02-08 11:00:00', '2024-02-08 13:46:00', 200.00, 290.00, NOW(), NOW()),
(3, 3, '2024-02-08 15:00:00', '2024-02-08 17:38:00', 180.00, 270.00, NOW(), NOW()),
(4, 3, '2024-02-08 19:00:00', '2024-02-08 21:44:00', 210.00, 310.00, NOW(), NOW()),
(1, 4, '2024-02-08 13:30:00', '2024-02-08 16:16:00', 250.00, 360.00, NOW(), NOW()),
(5, 4, '2024-02-08 18:00:00', '2024-02-08 20:38:00', 270.00, 390.00, NOW(), NOW()),

-- Hyderabad shows
(3, 5, '2024-02-08 10:00:00', '2024-02-08 12:38:00', 190.00, 280.00, NOW(), NOW()),
(4, 5, '2024-02-08 14:00:00', '2024-02-08 16:44:00', 220.00, 320.00, NOW(), NOW()),
(5, 5, '2024-02-08 18:30:00', '2024-02-08 21:08:00', 240.00, 350.00, NOW(), NOW()),
(1, 6, '2024-02-08 12:30:00', '2024-02-08 15:16:00', 230.00, 330.00, NOW(), NOW()),
(2, 6, '2024-02-08 16:30:00', '2024-02-08 19:16:00', 210.00, 310.00, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Ensure all shows have show_seats (this is critical!)
INSERT INTO show_seats (show_id, row_number, seat_number, category, status, price, created_at, updated_at)
SELECT 
    s.id as show_id,
    st.row_number,
    st.seat_number,
    st.category,
    'AVAILABLE' as status,
    CASE WHEN st.category = 'PREMIUM' THEN s.premium_price ELSE s.base_price END as price,
    NOW(),
    NOW()
FROM shows s
JOIN screens sc ON s.screen_id = sc.id
JOIN seats st ON st.screen_id = sc.id
WHERE NOT EXISTS (
    SELECT 1 FROM show_seats ss 
    WHERE ss.show_id = s.id 
    AND ss.row_number = st.row_number 
    AND ss.seat_number = st.seat_number
)
ON CONFLICT DO NOTHING;

-- Add some cinemas in other cities to ensure variety
INSERT INTO cinemas (name, address, city_id, theater_chain_id, created_at, updated_at) VALUES
('PVR Phoenix Market City', 'Phoenix Market City, Whitefield, Bangalore', 3, 1, NOW(), NOW()),
('INOX Mantri Square', 'Mantri Square, Malleshwaram, Bangalore', 3, 2, NOW(), NOW()),
('Cinepolis Orion Mall', 'Orion Mall, Rajajinagar, Bangalore', 3, 3, NOW(), NOW()),
('PVR Select City Walk', 'Select City Walk, Saket, Delhi', 2, 1, NOW(), NOW()),
('INOX Ambience Mall', 'Ambience Mall, Vasant Kunj, Delhi', 2, 2, NOW(), NOW()),
('PVR Phoenix Palladium', 'Phoenix Palladium, Mumbai', 1, 1, NOW(), NOW()),
('INOX Metro Big Cinemas', 'Metro Big Cinemas, Mumbai', 1, 2, NOW(), NOW()),
('Cinepolis Sathyam', 'Sathyam Cinemas, Chennai', 4, 3, NOW(), NOW()),
('PVR South City Mall', 'South City Mall, Kolkata', 5, 1, NOW(), NOW()),
('INOX Phoenix Marketcity', 'Phoenix Marketcity, Pune', 6, 2, NOW(), NOW()),
('Cinepolis Inorbit Mall', 'Inorbit Mall, Hyderabad', 7, 3, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Add more screens for the new cinemas
INSERT INTO screens (name, cinema_id, total_rows, seats_per_row, created_at, updated_at) VALUES
('Screen 1', 7, 12, 14, NOW(), NOW()),  -- 168 seats
('Screen 2', 7, 10, 16, NOW(), NOW()),  -- 160 seats
('Screen 1', 8, 14, 12, NOW(), NOW()),  -- 168 seats
('Screen 1', 9, 8, 18, NOW(), NOW()),   -- 144 seats
('Screen 2', 9, 10, 14, NOW(), NOW()),  -- 140 seats
('Screen 1', 10, 16, 10, NOW(), NOW()), -- 160 seats
('Screen 1', 11, 9, 16, NOW(), NOW()),   -- 144 seats
('Screen 2', 11, 12, 12, NOW(), NOW()),  -- 144 seats
('Screen 1', 12, 10, 15, NOW(), NOW()),  -- 150 seats
('Screen 1', 13, 11, 14, NOW(), NOW()),  -- 154 seats
('Screen 1', 14, 13, 12, NOW(), NOW()),  -- 156 seats
('Screen 2', 14, 9, 16, NOW(), NOW())    -- 144 seats
ON CONFLICT DO NOTHING;

-- Add seats for the new screens
INSERT INTO seats (screen_id, row_number, seat_number, category, created_at, updated_at)
SELECT 
    s.id as screen_id,
    n as row_number,
    m as seat_number,
    CASE WHEN n > s.total_rows - 3 THEN 'PREMIUM' ELSE 'REGULAR' END as category,
    NOW(),
    NOW()
FROM screens s
CROSS JOIN generate_series(1, s.total_rows) AS n(row_number)
CROSS JOIN generate_series(1, s.seats_per_row) AS m(seat_number)
WHERE s.id > 6  -- Only for new screens
ON CONFLICT DO NOTHING;

-- Verify all shows have seating plans by checking counts
DO $$
DECLARE
    show_count INTEGER;
    show_seat_count INTEGER;
    missing_seats INTEGER;
BEGIN
    SELECT COUNT(*) INTO show_count FROM shows;
    SELECT COUNT(*) INTO show_seat_count FROM show_seats;
    
    RAISE NOTICE 'Total shows: %', show_count;
    RAISE NOTICE 'Total show seats: %', show_seat_count;
    
    -- Check for shows without seats
    SELECT COUNT(*) INTO missing_seats
    FROM shows s
    WHERE NOT EXISTS (
        SELECT 1 FROM show_seats ss WHERE ss.show_id = s.id
    );
    
    IF missing_seats > 0 THEN
        RAISE NOTICE 'WARNING: % shows are missing seating plans!', missing_seats;
    ELSE
        RAISE NOTICE 'SUCCESS: All shows have seating plans!';
    END IF;
END $$;
