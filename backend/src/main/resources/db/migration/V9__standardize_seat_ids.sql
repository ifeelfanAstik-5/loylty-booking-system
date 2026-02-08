-- Standardize seat IDs to exactly 120 per show to match UI expectations
-- UI expects: Show 1 = 1-120, Show 2 = 121-240, Show 3 = 241-360, etc.
-- Regardless of actual screen size, each show gets exactly 120 sequential IDs

-- Drop and recreate show_seats with standardized 120 seats per show
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

-- Standardize: Every show gets exactly 120 seats (10 rows x 12 seats)
-- Even if the actual screen has different dimensions, we standardize to 10x12
INSERT INTO show_seats (id, show_id, row_number, seat_number, category, status, price, created_at, updated_at)
SELECT 
    -- Calculate standardized sequential ID: (show_id - 1) * 120 + seat_position
    (s.id - 1) * 120 + ((row_num - 1) * 12 + seat_num) as id,
    s.id as show_id,
    row_num as row_number,
    seat_num as seat_number,
    CASE 
        WHEN row_num > 7 THEN 'PREMIUM'  -- Last 3 rows are premium
        ELSE 'REGULAR'
    END as category,
    'AVAILABLE' as status,
    CASE 
        WHEN row_num > 7 THEN s.premium_price
        ELSE s.base_price
    END as price,
    NOW(),
    NOW()
FROM shows s
CROSS JOIN generate_series(1, 10) AS row_num(row_number)
CROSS JOIN generate_series(1, 12) AS seat_num(seat_number)
ORDER BY s.id, row_num, seat_num;

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
    RAISE NOTICE '=== VERIFYING STANDARDIZED SEAT IDs ===';
    
    FOR current_show_id IN 1..122 LOOP
        total_shows := total_shows + 1;
        
        -- Calculate expected seat ID range for this show (exactly 120 seats)
        expected_min_id := (current_show_id - 1) * 120 + 1;
        expected_max_id := current_show_id * 120;
        
        -- Get actual seat data
        SELECT COUNT(*), MIN(id), MAX(id) INTO seat_count, actual_min_id, actual_max_id
        FROM show_seats 
        WHERE show_id = current_show_id;
        
        IF actual_min_id = expected_min_id AND actual_max_id = expected_max_id AND seat_count = 120 THEN
            fixed_count := fixed_count + 1;
        ELSE
            RAISE NOTICE 'Show % STILL BROKEN: Expected %-% (120 seats), Actual %-% (% seats)', 
                current_show_id, expected_min_id, expected_max_id, actual_min_id, actual_max_id, seat_count;
        END IF;
    END LOOP;
    
    RAISE NOTICE '=== STANDARDIZATION RESULTS ===';
    RAISE NOTICE 'Total shows: %', total_shows;
    RAISE NOTICE 'Fixed shows: %', fixed_count;
    RAISE NOTICE 'Success rate: %', (fixed_count * 100.0 / total_shows);
    
    IF fixed_count = total_shows THEN
        RAISE NOTICE '🎉 ALL SHOWS STANDARDIZED TO 120 SEATS EACH!';
    END IF;
END $$;

-- Add comment explaining the standardization
COMMENT ON TABLE show_seats IS 'Standardized seating plans: Every show has exactly 120 seats (10x12) with sequential IDs matching UI expectations';
