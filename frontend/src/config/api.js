const API_BASE_URL = import.meta.env.VITE_API_URL || 'https://loylty-booking-production.up.railway.app/api';

export const API_ENDPOINTS = {
  // Cities
  CITIES: `${API_BASE_URL}/cities`,
  
  // Movies
  MOVIES_BY_CITY: (cityId) => `${API_BASE_URL}/movies/city/${cityId}`,
  SEARCH_MOVIES: `${API_BASE_URL}/movies/search`,
  
  // Shows
  SHOWS_BY_MOVIE_CITY: (movieId, cityId) => `${API_BASE_URL}/shows/movie/${movieId}/city/${cityId}`,
  SHOWS_GROUPED: (movieId, cityId) => `${API_BASE_URL}/shows/movie/${movieId}/city/${cityId}/grouped`,
  SHOW_BY_ID: (showId) => `${API_BASE_URL}/shows/${showId}`,
  
  // Seats
  SEAT_LAYOUT: (showId) => `${API_BASE_URL}/seats/show/${showId}/layout`,
  SEAT_STATUS: (showId) => `${API_BASE_URL}/seats/show/${showId}/status`,
  
  // Bookings
  LOCK_SEATS: `${API_BASE_URL}/bookings/lock-seats`,
  UNLOCK_SEATS: `${API_BASE_URL}/bookings/unlock-seats`,
  CREATE_BOOKING: `${API_BASE_URL}/bookings/create`,
  
  // Health
  HEALTH: `${API_BASE_URL}/health`
};

export default API_BASE_URL;
