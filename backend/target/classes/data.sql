-- Seed Data for Movie Booking System

-- Insert Cities
INSERT INTO cities (name, created_at, updated_at) VALUES 
('Mumbai', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Delhi', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Bangalore', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Chennai', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Kolkata', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert Theater Chains
INSERT INTO theater_chains (name, created_at, updated_at) VALUES 
('PVR Cinemas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('INOX', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Cinepolis', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Carnival Cinemas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert Cinemas
INSERT INTO cinemas (name, address, city_id, theater_chain_id, created_at, updated_at) VALUES 
('PVR Phoenix Marketcity', 'Phoenix Marketcity, LBS Marg, Kurla West, Mumbai', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('PVR Juhu', 'Juhu Tara Road, Juhu, Mumbai', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('INOX Nexus', 'Nexus Mall, 4th Floor, Whitefield, Bangalore', 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('INOX Garuda', 'Garuda Mall, Magrath Road, Bangalore', 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Cinepolis Select Citywalk', 'Select Citywalk, Saket, New Delhi', 2, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Cinepolis Viviana', 'Viviana Mall, Thane, Mumbai', 1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert Screens
INSERT INTO screens (name, cinema_id, total_rows, seats_per_row, created_at, updated_at) VALUES 
('Screen 1', 1, 10, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Screen 2', 1, 8, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Screen 1', 2, 12, 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Screen 1', 3, 10, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Screen 2', 3, 8, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Screen 1', 4, 10, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Screen 1', 5, 12, 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Screen 1', 6, 10, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert Seats (for Screen 1 at PVR Phoenix Marketcity - ID: 1)
INSERT INTO seats (screen_id, row_number, seat_number, category, created_at, updated_at) 
SELECT 
    1,
    generate_series,
    seat_num,
    CASE WHEN generate_series >= 9 THEN 'PREMIUM' ELSE 'REGULAR' END,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM generate_series(1, 10) row_numbers,
     generate_series(1, 12) seat_nums;

-- Insert Seats (for Screen 2 at PVR Phoenix Marketcity - ID: 2)
INSERT INTO seats (screen_id, row_number, seat_number, category, created_at, updated_at) 
SELECT 
    2,
    generate_series,
    seat_num,
    CASE WHEN generate_series >= 7 THEN 'PREMIUM' ELSE 'REGULAR' END,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM generate_series(1, 8) row_numbers,
     generate_series(1, 10) seat_nums;

-- Insert Seats (for Screen 1 at PVR Juhu - ID: 3)
INSERT INTO seats (screen_id, row_number, seat_number, category, created_at, updated_at) 
SELECT 
    3,
    generate_series,
    seat_num,
    CASE WHEN generate_series >= 11 THEN 'PREMIUM' ELSE 'REGULAR' END,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM generate_series(1, 12) row_numbers,
     generate_series(1, 14) seat_nums;

-- Insert Seats (for Screen 1 at INOX Nexus - ID: 4)
INSERT INTO seats (screen_id, row_number, seat_number, category, created_at, updated_at) 
SELECT 
    4,
    generate_series,
    seat_num,
    CASE WHEN generate_series >= 9 THEN 'PREMIUM' ELSE 'REGULAR' END,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM generate_series(1, 10) row_numbers,
     generate_series(1, 12) seat_nums;

-- Insert Movies
INSERT INTO movies (title, description, duration_minutes, language, genre, rating, release_date, created_at, updated_at) VALUES 
('Dunki', 'A heartwarming tale of four friends and their journey to foreign lands.', 160, 'Hindi', 'Drama/Comedy', 'UA', '2023-12-21', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Animal', 'A story about a father-son relationship filled with intense emotions.', 201, 'Hindi', 'Action/Drama', 'A', '2023-12-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Jawan', 'A high-octane action thriller with a social message.', 169, 'Hindi', 'Action/Thriller', 'UA', '2023-09-07', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Gadar 2', 'A sequel to the iconic film about love and patriotism.', 170, 'Hindi', 'Drama/Action', 'UA', '2023-08-11', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Oppenheimer', 'The story of the scientist who changed the world.', 180, 'English', 'Biography/Drama', 'UA', '2023-07-21', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Barbie', 'A colorful adventure in the world of Barbie.', 114, 'English', 'Comedy/Adventure', 'UA', '2023-07-21', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert Shows (for next few days)
INSERT INTO shows (movie_id, screen_id, show_time, end_time, base_price, premium_price, created_at, updated_at) VALUES 
-- Today's shows at PVR Phoenix Marketcity Screen 1
(1, 1, CURRENT_DATE + INTERVAL '09:00', CURRENT_DATE + INTERVAL '11:40', 250.00, 350.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 1, CURRENT_DATE + INTERVAL '12:00', CURRENT_DATE + INTERVAL '14:40', 250.00, 350.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 1, CURRENT_DATE + INTERVAL '15:00', CURRENT_DATE + INTERVAL '17:40', 280.00, 400.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 1, CURRENT_DATE + INTERVAL '18:00', CURRENT_DATE + INTERVAL '20:40', 320.00, 450.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 1, CURRENT_DATE + INTERVAL '21:00', CURRENT_DATE + INTERVAL '23:40', 350.00, 500.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Today's shows at PVR Phoenix Marketcity Screen 2
(2, 2, CURRENT_DATE + INTERVAL '10:00', CURRENT_DATE + INTERVAL '13:21', 250.00, 350.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, CURRENT_DATE + INTERVAL '13:30', CURRENT_DATE + INTERVAL '16:51', 280.00, 400.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, CURRENT_DATE + INTERVAL '17:00', CURRENT_DATE + INTERVAL '20:21', 320.00, 450.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Today's shows at PVR Juhu Screen 1
(3, 3, CURRENT_DATE + INTERVAL '11:00', CURRENT_DATE + INTERVAL '13:49', 250.00, 350.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 3, CURRENT_DATE + INTERVAL '14:00', CURRENT_DATE + INTERVAL '16:49', 280.00, 400.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 3, CURRENT_DATE + INTERVAL '17:00', CURRENT_DATE + INTERVAL '19:49', 320.00, 450.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 3, CURRENT_DATE + INTERVAL '20:00', CURRENT_DATE + INTERVAL '22:49', 350.00, 500.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Today's shows at INOX Nexus Screen 1
(4, 4, CURRENT_DATE + INTERVAL '09:30', CURRENT_DATE + INTERVAL '12:20', 250.00, 350.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 4, CURRENT_DATE + INTERVAL '12:30', CURRENT_DATE + INTERVAL '15:20', 280.00, 400.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 4, CURRENT_DATE + INTERVAL '15:30', CURRENT_DATE + INTERVAL '18:20', 320.00, 450.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 4, CURRENT_DATE + INTERVAL '18:30', CURRENT_DATE + INTERVAL '21:20', 350.00, 500.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Tomorrow's shows at PVR Phoenix Marketcity Screen 1
(5, 1, CURRENT_DATE + INTERVAL '1 day' + INTERVAL '09:00', CURRENT_DATE + INTERVAL '1 day' + INTERVAL '11:40', 250.00, 350.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 1, CURRENT_DATE + INTERVAL '1 day' + INTERVAL '12:00', CURRENT_DATE + INTERVAL '1 day' + INTERVAL '14:40', 250.00, 350.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 1, CURRENT_DATE + INTERVAL '1 day' + INTERVAL '15:00', CURRENT_DATE + INTERVAL '1 day' + INTERVAL '17:40', 280.00, 400.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 1, CURRENT_DATE + INTERVAL '1 day' + INTERVAL '18:00', CURRENT_DATE + INTERVAL '1 day' + INTERVAL '20:40', 320.00, 450.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 1, CURRENT_DATE + INTERVAL '1 day' + INTERVAL '21:00', CURRENT_DATE + INTERVAL '1 day' + INTERVAL '23:40', 350.00, 500.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Tomorrow's shows at PVR Phoenix Marketcity Screen 2
(6, 2, CURRENT_DATE + INTERVAL '1 day' + INTERVAL '10:00', CURRENT_DATE + INTERVAL '1 day' + INTERVAL '13:21', 250.00, 350.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 2, CURRENT_DATE + INTERVAL '1 day' + INTERVAL '13:30', CURRENT_DATE + INTERVAL '1 day' + INTERVAL '16:51', 280.00, 400.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 2, CURRENT_DATE + INTERVAL '1 day' + INTERVAL '17:00', CURRENT_DATE + INTERVAL '1 day' + INTERVAL '20:21', 320.00, 450.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
