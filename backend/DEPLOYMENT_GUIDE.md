# Railway Deployment Guide

## Current Configuration

The backend is configured for Railway deployment with the following setup:

### Files:
- `railway.toml` - Railway configuration
- `Dockerfile` - Multi-stage Docker build
- `application-prod.properties` - Production configuration
- `V2__add_show_seats.sql` - Database migration for new ShowSeat system

### Railway Configuration:
- **Builder**: Dockerfile
- **Health Check**: `/api/health`
- **Profile**: Production (`prod`)
- **Database**: Railway PostgreSQL addon

## New Features Deployed:

### 1. Real-World Theater Seating Logic
- Each show has its own immutable seating plan
- Seats created automatically when shows are created
- Seat categories: REGULAR, PREMIUM (last 3 rows)
- Seat states: AVAILABLE → LOCKED → BOOKED

### 2. Enhanced Seat Management
- Temporary seat locking (5 minutes)
- Automatic cleanup of expired locks
- Real-time seat availability tracking
- Premium seat pricing

### 3. API Endpoints
- `GET /api/shows/{id}/seats` - Get seat layout with status
- `POST /api/bookings/lock-seats` - Lock seats for booking
- `POST /api/internal/shows/cleanup-expired-locks` - Clean expired locks

## Database Migration

The V2 migration safely adds the new `show_seats` table:
- Creates table with proper constraints
- Populates with existing show data
- Adds performance indexes
- Preserves existing data

## Deployment Steps:

1. **Push to Railway**: The Railway GitHub integration will automatically deploy
2. **Database Migration**: Flyway will run V2 migration automatically
3. **Health Check**: Railway will monitor `/api/health`
4. **Environment Variables**: Railway provides DATABASE_URL automatically

## Production Database Setup:

The migration will:
1. Create `show_seats` table
2. Populate with 1,800 seats (120 seats × 15 existing shows)
3. Set up proper indexes and triggers
4. Maintain backward compatibility

## Verification:

After deployment, test:
- Health check: `GET /api/health`
- Seat layout: `GET /api/shows/1/seats`
- Seat locking: `POST /api/bookings/lock-seats`

## Notes:

- The deployment is backward compatible
- Existing booking system continues to work
- New ShowSeat system provides enhanced functionality
- Database migration is safe and reversible
