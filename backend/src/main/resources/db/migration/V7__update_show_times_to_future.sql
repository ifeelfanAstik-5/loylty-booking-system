-- Update show times to be in the future for proper testing
-- This ensures shows appear in the API responses

UPDATE shows 
SET show_time = CASE 
    WHEN id <= 14 THEN '2024-02-09 09:00:00' + INTERVAL '1 hour' * (id - 1)
    WHEN id <= 28 THEN '2024-02-09 14:00:00' + INTERVAL '1 hour' * (id - 15)
    WHEN id <= 42 THEN '2024-02-09 18:00:00' + INTERVAL '1 hour' * (id - 29)
    ELSE '2024-02-09 20:00:00' + INTERVAL '1 hour' * (id - 43)
END,
end_time = CASE 
    WHEN id <= 14 THEN '2024-02-09 11:46:00' + INTERVAL '1 hour' * (id - 1)
    WHEN id <= 28 THEN '2024-02-09 16:46:00' + INTERVAL '1 hour' * (id - 15)
    WHEN id <= 42 THEN '2024-02-09 20:46:00' + INTERVAL '1 hour' * (id - 29)
    ELSE '2024-02-09 22:46:00' + INTERVAL '1 hour' * (id - 43)
END,
updated_at = NOW()
WHERE show_time < CURRENT_TIMESTAMP;

-- Add some shows for tomorrow as well
INSERT INTO shows (movie_id, screen_id, show_time, end_time, base_price, premium_price, created_at, updated_at) VALUES
-- Tomorrow shows in Bangalore
(1, 1, '2024-02-10 10:00:00', '2024-02-10 12:46:00', 280.00, 400.00, NOW(), NOW()),
(2, 1, '2024-02-10 14:00:00', '2024-02-10 16:46:00', 260.00, 380.00, NOW(), NOW()),
(3, 1, '2024-02-10 18:00:00', '2024-02-10 20:38:00', 240.00, 350.00, NOW(), NOW()),
(4, 2, '2024-02-10 11:00:00', '2024-02-10 13:44:00', 300.00, 450.00, NOW(), NOW()),
(5, 2, '2024-02-10 15:00:00', '2024-02-10 17:38:00', 320.00, 480.00, NOW(), NOW()),

-- Tomorrow shows in Delhi
(1, 4, '2024-02-10 09:30:00', '2024-02-10 12:16:00', 290.00, 420.00, NOW(), NOW()),
(2, 4, '2024-02-10 13:30:00', '2024-02-10 16:16:00', 270.00, 390.00, NOW(), NOW()),
(3, 4, '2024-02-10 17:30:00', '2024-02-10 20:08:00', 250.00, 360.00, NOW(), NOW()),
(4, 5, '2024-02-10 10:30:00', '2024-02-10 13:14:00', 310.00, 460.00, NOW(), NOW()),
(5, 5, '2024-02-10 14:30:00', '2024-02-10 17:14:00', 330.00, 490.00, NOW(), NOW()),

-- Tomorrow shows in Mumbai
(1, 1, '2024-02-10 11:30:00', '2024-02-10 14:16:00', 260.00, 370.00, NOW(), NOW()),
(2, 1, '2024-02-10 15:30:00', '2024-02-10 18:16:00', 240.00, 350.00, NOW(), NOW()),
(3, 1, '2024-02-10 19:30:00', '2024-02-10 22:08:00', 220.00, 320.00, NOW(), NOW()),
(4, 2, '2024-02-10 10:00:00', '2024-02-10 12:44:00', 290.00, 420.00, NOW(), NOW()),
(5, 2, '2024-02-10 14:00:00', '2024-02-10 16:38:00', 310.00, 450.00, NOW(), NOW()),

-- Tomorrow shows in Chennai
(2, 3, '2024-02-10 09:00:00', '2024-02-10 11:44:00', 230.00, 330.00, NOW(), NOW()),
(3, 3, '2024-02-10 13:00:00', '2024-02-10 15:38:00', 210.00, 310.00, NOW(), NOW()),
(4, 3, '2024-02-10 17:00:00', '2024-02-10 19:44:00', 240.00, 350.00, NOW(), NOW()),
(5, 3, '2024-02-10 20:30:00', '2024-02-10 23:08:00', 260.00, 380.00, NOW(), NOW()),

-- Tomorrow shows in Kolkata
(1, 4, '2024-02-10 12:00:00', '2024-02-10 14:46:00', 250.00, 360.00, NOW(), NOW()),
(2, 4, '2024-02-10 16:00:00', '2024-02-10 18:46:00', 230.00, 330.00, NOW(), NOW()),
(3, 4, '2024-02-10 20:00:00', '2024-02-10 22:38:00', 210.00, 310.00, NOW(), NOW()),

-- Tomorrow shows in Pune
(2, 5, '2024-02-10 10:30:00', '2024-02-10 13:16:00', 200.00, 290.00, NOW(), NOW()),
(3, 5, '2024-02-10 14:30:00', '2024-02-10 17:08:00', 180.00, 270.00, NOW(), NOW()),
(4, 5, '2024-02-10 18:30:00', '2024-02-10 21:14:00', 210.00, 310.00, NOW(), NOW()),

-- Tomorrow shows in Hyderabad
(3, 6, '2024-02-10 09:30:00', '2024-02-10 12:08:00', 190.00, 280.00, NOW(), NOW()),
(4, 6, '2024-02-10 13:30:00', '2024-02-10 16:14:00', 220.00, 320.00, NOW(), NOW()),
(5, 6, '2024-02-10 17:30:00', '2024-02-10 20:08:00', 240.00, 350.00, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- Create show seats for the new shows
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
WHERE s.id > (SELECT COALESCE(MAX(id), 0) FROM shows WHERE id <= 47)
AND NOT EXISTS (
    SELECT 1 FROM show_seats ss 
    WHERE ss.show_id = s.id 
    AND ss.row_number = st.row_number 
    AND ss.seat_number = st.seat_number
);

-- Verify the results
DO $$
DECLARE
    total_shows INTEGER;
    future_shows INTEGER;
    total_show_seats INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_shows FROM shows;
    SELECT COUNT(*) INTO future_shows FROM shows WHERE show_time > CURRENT_TIMESTAMP;
    SELECT COUNT(*) INTO total_show_seats FROM show_seats;
    
    RAISE NOTICE 'Total shows: %', total_shows;
    RAISE NOTICE 'Future shows: %', future_shows;
    RAISE NOTICE 'Total show seats: %', total_show_seats;
    
    IF future_shows = 0 THEN
        RAISE NOTICE 'WARNING: No future shows found!';
    ELSE
        RAISE NOTICE 'SUCCESS: Found % future shows!', future_shows;
    END IF;
END $$;
