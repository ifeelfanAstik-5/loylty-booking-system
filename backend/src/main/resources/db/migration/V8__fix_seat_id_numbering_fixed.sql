-- Fix seat ID numbering to match UI expectations
-- UI expects sequential seat IDs: Show 1 = 1-120, Show 2 = 121-240, Show 3 = 241-360, etc.

-- Drop existing show_seats to recreate with correct numbering
DROP TABLE IF EXISTS show_seats CASCADE;

-- Recreate show_seats table
CREATE TABLE show_seats (
    id BIGSERIAL PRIMARY KEY,
    show_id BIGINT NOT NULL REFERENCES shows(id) ON DELETE CASCADE,
    row_number INTEGER NOT NULL,
    seat_number INTEGER NOT NULL,
    category VARCHAR(20) NOT NULL DEFAULT 'REGULAR' CHECK (category IN ('REGULAR', 'PREMIUM', 'VIP')),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'LOCKED', 'BOOKED')),
    price DECIMAL(10,2) NOT NULL CHECK (price > 0),
    lock_user_id VARCHAR(255),
    lock_expiry_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(show_id, row_number, seat_number)
);

-- Create indexes
CREATE INDEX idx_show_seats_show_id ON show_seats(show_id);
CREATE INDEX idx_show_seats_status ON show_seats(status);
CREATE INDEX idx_show_seats_lock_expiry ON show_seats(lock_expiry_time);

-- Create trigger for updated_at
CREATE OR REPLACE FUNCTION update_show_seats_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_show_seats_updated_at BEFORE UPDATE ON show_seats
    FOR EACH ROW EXECUTE FUNCTION update_show_seats_updated_at();

-- Now populate show_seats with CORRECT sequential IDs
-- This ensures Show 1 has seats 1-120, Show 2 has 121-240, etc.

INSERT INTO show_seats (id, show_id, row_number, seat_number, category, status, price, created_at, updated_at)
SELECT 
    -- Calculate the correct sequential ID
    (s.id - 1) * 120 + (st.row_number - 1) * sc.seats_per_row + st.seat_number as id,
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
ORDER BY s.id, st.row_number, st.seat_number;

-- Verify the fix
DO $$
DECLARE
    current_show_id BIGINT;
    expected_min_id BIGINT;
    expected_max_id BIGINT;
    actual_min_id BIGINT;
    actual_max_id BIGINT;
    seat_count BIGINT;
    fixed_count INTEGER := 0;
    total_shows INTEGER := 0;
BEGIN
    RAISE NOTICE '=== VERIFYING SEAT ID FIX ===';
    
    FOR current_show_id IN 1..122 LOOP
        total_shows := total_shows + 1;
        
        -- Calculate expected seat ID range for this show
        expected_min_id := (current_show_id - 1) * 120 + 1;
        expected_max_id := current_show_id * 120;
        
        -- Get actual seat data
        SELECT COUNT(*), MIN(id), MAX(id) INTO seat_count, actual_min_id, actual_max_id
        FROM show_seats 
        WHERE show_id = current_show_id;
        
        IF actual_min_id = expected_min_id AND actual_max_id = expected_max_id AND seat_count > 0 THEN
            fixed_count := fixed_count + 1;
        ELSE
            RAISE NOTICE 'Show % STILL BROKEN: Expected %-%, Actual %-% (Count: %)', 
                current_show_id, expected_min_id, expected_max_id, actual_min_id, actual_max_id, seat_count;
        END IF;
    END LOOP;
    
    RAISE NOTICE '=== FIX RESULTS ===';
    RAISE NOTICE 'Total shows: %', total_shows;
    RAISE NOTICE 'Fixed shows: %', fixed_count;
    RAISE NOTICE 'Success rate: %', (fixed_count * 100.0 / total_shows);
    
    IF fixed_count = total_shows THEN
        RAISE NOTICE '🎉 ALL SHOWS FIXED SUCCESSFULLY!';
    END IF;
END $$;

-- Add comment explaining the fix
COMMENT ON TABLE show_seats IS 'Show-specific seating plans with sequential IDs matching UI expectations (Show 1: 1-120, Show 2: 121-240, etc.)';
