-- Fix booking_seats table schema to use seat_id instead of show_seat_id
-- This migration updates the table structure to match the new BookingSeat entity

-- Check if the column exists and rename it if needed
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'booking_seats' 
               AND column_name = 'show_seat_id') THEN
        -- Add new seat_id column
        ALTER TABLE booking_seats ADD COLUMN seat_id BIGINT;
        
        -- Copy data from show_seat_id to seat_id if there's any data
        UPDATE booking_seats SET seat_id = show_seat_id WHERE seat_id IS NULL;
        
        -- Drop the old column
        ALTER TABLE booking_seats DROP COLUMN show_seat_id;
        
        RAISE NOTICE 'Migrated booking_seats table from show_seat_id to seat_id';
    END IF;
END $$;

-- Make sure seat_id is not nullable and has proper constraints
ALTER TABLE booking_seats ALTER COLUMN seat_id SET NOT NULL;

-- Update unique constraint if needed
DROP TABLE IF EXISTS booking_seats CASCADE;
CREATE TABLE booking_seats (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    UNIQUE(booking_id, seat_id)
);

-- Create indexes
CREATE INDEX idx_booking_seats_booking_id ON booking_seats(booking_id);
CREATE INDEX idx_booking_seats_seat_id ON booking_seats(seat_id);

COMMENT ON TABLE booking_seats IS 'Booking seats with direct seat ID reference for in-memory system';
