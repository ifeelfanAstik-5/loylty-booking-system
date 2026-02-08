-- Add show_seats table for real-world theater seating logic
-- This migration adds the new ShowSeat functionality while preserving existing data

-- Create show_seats table
CREATE TABLE IF NOT EXISTS show_seats (
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

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_show_seats_show_id ON show_seats(show_id);
CREATE INDEX IF NOT EXISTS idx_show_seats_status ON show_seats(status);
CREATE INDEX IF NOT EXISTS idx_show_seats_lock_expiry ON show_seats(lock_expiry_time);

-- Create trigger for updated_at
CREATE OR REPLACE FUNCTION update_show_seats_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

DROP TRIGGER IF EXISTS update_show_seats_updated_at ON show_seats;
CREATE TRIGGER update_show_seats_updated_at BEFORE UPDATE ON show_seats
    FOR EACH ROW EXECUTE FUNCTION update_show_seats_updated_at();

-- Populate show_seats with data from existing shows
-- Only insert if the table is empty
INSERT INTO show_seats (show_id, row_number, seat_number, category, status, price, created_at, updated_at)
SELECT 
    s.id as show_id,
    st.row_number,
    st.seat_number,
    CASE WHEN st.row_number > sc.total_rows - 3 THEN 'PREMIUM' ELSE 'REGULAR' END as category,
    'AVAILABLE' as status,
    CASE WHEN st.row_number > sc.total_rows - 3 THEN s.premium_price ELSE s.base_price END as price,
    NOW() as created_at,
    NOW() as updated_at
FROM shows s
JOIN screens sc ON s.screen_id = sc.id
JOIN seats st ON st.screen_id = sc.id
WHERE NOT EXISTS (SELECT 1 FROM show_seats LIMIT 1);

-- Update booking_seats to reference show_seats instead of seats
-- This is a more complex migration that would need to be done carefully in production
-- For now, we'll keep the existing booking_seats structure and handle it in the application

-- Add a comment to document the migration
COMMENT ON TABLE show_seats IS 'Show-specific seating plan with real-world theater logic';
