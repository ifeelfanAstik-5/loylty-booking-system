# Movie Ticket Booking System

A comprehensive movie ticket booking system built with Java Spring Boot backend and React Vite frontend.

## Architecture

- **Backend**: Java 17 + Spring Boot 3.2.0 + PostgreSQL
- **Frontend**: React + Vite
- **Database**: PostgreSQL
- **Cache**: In-memory (extensible to Redis)

## Project Structure

```
movie-booking-system/
├── backend/          # Java Spring Boot API
├── frontend/         # React Vite application
├── pom.xml          # Maven parent POM
└── README.md
```

## Features

- City-based movie browsing
- Cinema and showtime selection
- Interactive seat selection with real-time availability
- 5-minute seat locking mechanism
- Guest booking support
- Multiple theater chains
- Seat categories (Regular, Premium)
- Admin APIs for content management

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- Node.js 16+
- PostgreSQL 14+

### Backend Setup

1. Navigate to backend directory:
```bash
cd backend
```

2. Configure database in `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/movie_booking
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. Run the application:
```bash
mvn spring-boot:run
```

### Frontend Setup

1. Navigate to frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start development server:
```bash
npm run dev
```

## API Endpoints

### Public APIs
- `GET /api/cities` - Get all cities
- `GET /api/cities/{cityId}/shows` - Get shows by city
- `GET /api/shows/{showId}/cinemas` - Get cinemas with showtimes
- `GET /api/shows/{showId}/seats` - Get seat layout and availability
- `POST /api/bookings/lock-seats` - Lock seats for 5 minutes
- `POST /api/bookings/confirm` - Confirm booking

### Admin APIs
- `POST /api/admin/cities` - Create city
- `POST /api/admin/theater-chains` - Create theater chain
- `POST /api/admin/cinemas` - Create cinema
- `POST /api/admin/movies` - Create movie
- `POST /api/admin/shows` - Create show

## Database Schema

The system uses the following main entities:
- Cities
- Theater Chains
- Cinemas
- Screens
- Seats (with categories)
- Movies
- Shows
- Bookings

## Deployment

- **Backend**: Railway
- **Frontend**: Vercel
- **Database**: Neon

## Development

The project follows a monorepo structure with Maven managing the backend and npm managing the frontend.
