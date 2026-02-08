-- Reset database and create proper show_seats table
-- This migration will be used for production reset

-- Drop existing tables in correct order (respecting foreign key constraints)
DROP TABLE IF EXISTS booking_seats CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS show_seat_states CASCADE;
DROP TABLE IF EXISTS shows CASCADE;
DROP TABLE IF EXISTS seats CASCADE;
DROP TABLE IF EXISTS screens CASCADE;
DROP TABLE IF EXISTS cinemas CASCADE;
DROP TABLE IF EXISTS movies CASCADE;
DROP TABLE IF EXISTS cities CASCADE;
DROP TABLE IF EXISTS theater_chains CASCADE;

-- Create theater_chains table
CREATE TABLE theater_chains (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Create cities table
CREATE TABLE cities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Create cinemas table
CREATE TABLE cinemas (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    city_id BIGINT NOT NULL REFERENCES cities(id),
    theater_chain_id BIGINT NOT NULL REFERENCES theater_chains(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Create movies table
CREATE TABLE movies (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    duration_minutes INTEGER NOT NULL,
    language VARCHAR(50) NOT NULL,
    genre VARCHAR(100),
    rating VARCHAR(10),
    release_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Create screens table
CREATE TABLE screens (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    cinema_id BIGINT NOT NULL REFERENCES cinemas(id),
    total_rows INTEGER NOT NULL CHECK (total_rows > 0),
    seats_per_row INTEGER NOT NULL CHECK (seats_per_row > 0),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Create shows table
CREATE TABLE shows (
    id BIGSERIAL PRIMARY KEY,
    movie_id BIGINT NOT NULL REFERENCES movies(id),
    screen_id BIGINT NOT NULL REFERENCES screens(id),
    show_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    base_price DECIMAL(10,2) NOT NULL CHECK (base_price > 0),
    premium_price DECIMAL(10,2) NOT NULL CHECK (premium_price > 0),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Create show_seats table (THIS IS THE KEY TABLE)
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

-- Create bookings table
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    show_id BIGINT NOT NULL REFERENCES shows(id),
    guest_name VARCHAR(100) NOT NULL,
    guest_email VARCHAR(100) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount > 0),
    booking_time TIMESTAMP NOT NULL DEFAULT NOW(),
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Create booking_seats table
CREATE TABLE booking_seats (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id),
    show_seat_id BIGINT NOT NULL REFERENCES show_seats(id),
    price DECIMAL(10,2) NOT NULL CHECK (price > 0),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(booking_id, show_seat_id)
);

-- Create indexes for performance
CREATE INDEX idx_cities_name ON cities(name);
CREATE INDEX idx_cinemas_city_id ON cinemas(city_id);
CREATE INDEX idx_movies_title ON movies(title);
CREATE INDEX idx_screens_cinema_id ON screens(cinema_id);
CREATE INDEX idx_shows_movie_id ON shows(movie_id);
CREATE INDEX idx_shows_screen_id ON shows(screen_id);
CREATE INDEX idx_shows_show_time ON shows(show_time);
CREATE INDEX idx_show_seats_show_id ON show_seats(show_id);
CREATE INDEX idx_show_seats_status ON show_seats(status);
CREATE INDEX idx_show_seats_lock_expiry ON show_seats(lock_expiry_time);
CREATE INDEX idx_bookings_show_id ON bookings(show_id);
CREATE INDEX idx_booking_seats_booking_id ON booking_seats(booking_id);

-- Create triggers for updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_theater_chains_updated_at BEFORE UPDATE ON theater_chains
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_cities_updated_at BEFORE UPDATE ON cities
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_cinemas_updated_at BEFORE UPDATE ON cinemas
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_movies_updated_at BEFORE UPDATE ON movies
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_screens_updated_at BEFORE UPDATE ON screens
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_shows_updated_at BEFORE UPDATE ON shows
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_show_seats_updated_at BEFORE UPDATE ON show_seats
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_bookings_updated_at BEFORE UPDATE ON bookings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Seed data
-- Theater chains
INSERT INTO theater_chains (name, description) VALUES
('PVR Cinemas', 'Leading multiplex chain in India'),
('INOX Cinemas', 'Premium cinema experience'),
('Cinepolis', 'International cinema chain');

-- Cities
INSERT INTO cities (name) VALUES
('Mumbai', 'Delhi', 'Bangalore', 'Chennai', 'Kolkata', 'Pune', 'Hyderabad');

-- Cinemas
INSERT INTO cinemas (name, address, city_id, theater_chain_id) VALUES
('PVR Phoenix Mall', 'Phoenix Mall, Whitefield, Bangalore', 3, 1),
('INOX Nexus Mall', 'Nexus Mall, Koramangala, Bangalore', 3, 2),
('Cinepolis Forum Mall', 'Forum Mall, Koramangala, Bangalore', 3, 3),
('PVR Select Citywalk', 'Select Citywalk, Saket, Delhi', 2, 1),
('INOX Ambience Mall', 'Ambience Mall, Vasant Kunj, Delhi', 2, 2);

-- Movies
INSERT INTO movies (title, description, duration_minutes, language, genre, rating, release_date) VALUES
('Dune: Part Two', 'Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.', 166, 'English', 'Sci-Fi', 'UA', '2024-03-01'),
('Fighter', 'Top IAF aviators come together in the face of imminent dangers, to form Air Dragons.', 166, 'Hindi', 'Action', 'UA', '2024-01-25'),
('Hanu-Man', 'Hanumanthu gets endowed with the powers of Hanuman and fights for Anjanadri.', 158, 'Telugu', 'Fantasy', 'UA', '2024-01-12');

-- Screens
INSERT INTO screens (name, cinema_id, total_rows, seats_per_row) VALUES
('Screen 1', 1, 10, 12),  -- 120 seats
('Screen 2', 1, 8, 14),   -- 112 seats
('Screen 3', 2, 12, 10),  -- 120 seats
('Screen 1', 3, 15, 8),   -- 120 seats
('Screen 2', 4, 10, 12),  -- 120 seats
('Screen 1', 5, 8, 15);   -- 120 seats

-- Shows (for next 7 days)
INSERT INTO shows (movie_id, screen_id, show_time, end_time, base_price, premium_price) VALUES
-- Dune: Part Two shows
(1, 1, '2024-02-08 09:00:00', '2024-02-08 11:46:00', 250.00, 350.00),
(1, 1, '2024-02-08 14:00:00', '2024-02-08 16:46:00', 250.00, 350.00),
(1, 1, '2024-02-08 18:30:00', '2024-02-08 21:16:00', 280.00, 400.00),
(1, 2, '2024-02-08 11:00:00', '2024-02-08 13:46:00', 220.00, 320.00),
(1, 3, '2024-02-08 15:00:00', '2024-02-08 17:46:00', 240.00, 340.00),

-- Fighter shows
(2, 4, '2024-02-08 10:00:00', '2024-02-08 12:46:00', 200.00, 300.00),
(2, 4, '2024-02-08 16:00:00', '2024-02-08 18:46:00', 220.00, 320.00),
(2, 5, '2024-02-08 13:00:00', '2024-02-08 15:46:00', 180.00, 280.00),

-- Hanu-Man shows
(3, 6, '2024-02-08 11:30:00', '2024-02-08 14:08:00', 150.00, 220.00),
(3, 6, '2024-02-08 17:00:00', '2024-02-08 19:38:00', 170.00, 250.00);

-- Show seats will be automatically created by the ShowSeatService when shows are created
-- This is the key real-world logic: each show has its own immutable seating plan
