-- Seed Production Data for Real-World Theater Seating Logic
-- This migration creates all necessary base data and show seats

-- Theater chains
INSERT INTO theater_chains (name, description, created_at, updated_at) VALUES
('PVR Cinemas', 'Leading multiplex chain in India', NOW(), NOW()),
('INOX Cinemas', 'Premium cinema experience', NOW(), NOW()),
('Cinepolis', 'International cinema chain', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Cities
INSERT INTO cities (name, created_at, updated_at) VALUES
('Mumbai', NOW(), NOW()),
('Delhi', NOW(), NOW()),
('Bangalore', NOW(), NOW()),
('Chennai', NOW(), NOW()),
('Kolkata', NOW(), NOW()),
('Pune', NOW(), NOW()),
('Hyderabad', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Cinemas
INSERT INTO cinemas (name, address, city_id, theater_chain_id, created_at, updated_at) VALUES
('PVR Phoenix Mall', 'Phoenix Mall, Whitefield, Bangalore', 3, 1, NOW(), NOW()),
('INOX Nexus Mall', 'Nexus Mall, Koramangala, Bangalore', 3, 2, NOW(), NOW()),
('Cinepolis Forum Mall', 'Forum Mall, Koramangala, Bangalore', 3, 3, NOW(), NOW()),
('PVR Select Citywalk', 'Select Citywalk, Saket, Delhi', 2, 1, NOW(), NOW()),
('INOX Ambience Mall', 'Ambience Mall, Vasant Kunj, Delhi', 2, 2, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Movies
INSERT INTO movies (title, description, duration_minutes, language, genre, rating, release_date, created_at, updated_at) VALUES
('Dune: Part Two', 'Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.', 166, 'English', 'Sci-Fi', 'UA', '2024-03-01', NOW(), NOW()),
('Fighter', 'Top IAF aviators come together in the face of imminent dangers, to form Air Dragons.', 166, 'Hindi', 'Action', 'UA', '2024-01-25', NOW(), NOW()),
('Hanu-Man', 'Hanumanthu gets endowed with the powers of Hanuman and fights for Anjanadri.', 158, 'Telugu', 'Fantasy', 'UA', '2024-01-12', NOW(), NOW()),
('Jawan', 'A high-ranking officer in the Indian army embarks on a mission to dismantle a terrorist organization.', 164, 'Hindi', 'Action', 'UA', '2023-09-07', NOW(), NOW()),
('Oppenheimer', 'The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb.', 180, 'English', 'Biography', 'UA', '2023-07-21', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Screens
INSERT INTO screens (name, cinema_id, total_rows, seats_per_row, created_at, updated_at) VALUES
('Screen 1', 1, 10, 12, NOW(), NOW()),  -- 120 seats
('Screen 2', 1, 8, 14, NOW(), NOW()),   -- 112 seats
('Screen 3', 2, 12, 10, NOW(), NOW()),  -- 120 seats
('Screen 1', 3, 15, 8, NOW(), NOW()),   -- 120 seats
('Screen 2', 4, 10, 12, NOW(), NOW()),  -- 120 seats
('Screen 1', 5, 8, 15, NOW(), NOW())    -- 120 seats
ON CONFLICT DO NOTHING;

-- Seats (physical seats in screens)
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
ON CONFLICT DO NOTHING;

-- Shows
INSERT INTO shows (movie_id, screen_id, show_time, end_time, base_price, premium_price, created_at, updated_at) VALUES
-- Dune: Part Two shows
(1, 1, '2024-02-08 09:00:00', '2024-02-08 11:46:00', 250.00, 350.00, NOW(), NOW()),
(1, 1, '2024-02-08 14:00:00', '2024-02-08 16:46:00', 250.00, 350.00, NOW(), NOW()),
(1, 1, '2024-02-08 18:30:00', '2024-02-08 21:16:00', 280.00, 400.00, NOW(), NOW()),
(1, 2, '2024-02-08 11:00:00', '2024-02-08 13:46:00', 220.00, 320.00, NOW(), NOW()),
(1, 3, '2024-02-08 15:00:00', '2024-02-08 17:46:00', 240.00, 340.00, NOW(), NOW()),

-- Fighter shows
(2, 4, '2024-02-08 10:00:00', '2024-02-08 12:46:00', 200.00, 300.00, NOW(), NOW()),
(2, 4, '2024-02-08 16:00:00', '2024-02-08 18:46:00', 220.00, 320.00, NOW(), NOW()),
(2, 5, '2024-02-08 13:00:00', '2024-02-08 15:46:00', 180.00, 280.00, NOW(), NOW()),

-- Hanu-Man shows
(3, 6, '2024-02-08 11:30:00', '2024-02-08 14:08:00', 150.00, 220.00, NOW(), NOW()),
(3, 6, '2024-02-08 17:00:00', '2024-02-08 19:38:00', 170.00, 250.00, NOW(), NOW()),

-- Jawan shows
(4, 1, '2024-02-08 09:30:00', '2024-02-08 12:14:00', 260.00, 380.00, NOW(), NOW()),
(4, 1, '2024-02-08 15:30:00', '2024-02-08 18:14:00', 280.00, 400.00, NOW(), NOW()),

-- Oppenheimer shows
(5, 2, '2024-02-08 13:00:00', '2024-02-08 16:00:00', 300.00, 450.00, NOW(), NOW()),
(5, 3, '2024-02-08 19:00:00', '2024-02-08 22:00:00', 320.00, 480.00, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Show Seats - This is the key part that creates seating plans for each show
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
WHERE NOT EXISTS (SELECT 1 FROM show_seats WHERE show_id = s.id AND row_number = st.row_number AND seat_number = st.seat_number)
ON CONFLICT DO NOTHING;

-- Add comments for documentation
COMMENT ON TABLE show_seats IS 'Show-specific seating plans with real-world theater logic - each show has its own immutable seats';
COMMENT ON TABLE seats IS 'Physical seats in cinema screens - base seating layout';
COMMENT ON TABLE shows IS 'Movie showtimes with pricing information';
