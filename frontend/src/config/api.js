const API_BASE_URL = import.meta.env.VITE_API_URL || 'https://loylty-booking-production.up.railway.app/api';

// Timezone offset middleware - convert UTC to IST
const adjustForTimezone = (data) => {
  if (!data) return data;
  
  // Handle different data types
  if (Array.isArray(data)) {
    return data.map(item => adjustForTimezone(item));
  }
  
  if (typeof data === 'object' && data !== null) {
    const adjusted = { ...data };
    
    // Adjust timestamp fields
    if (adjusted.timestamp) {
      adjusted.timestamp = new Date(new Date(adjusted.timestamp).getTime() + (5.5 * 60 * 60 * 1000)).toISOString();
    }
    
    // Adjust date/time fields
    if (adjusted.showTime) {
      adjusted.showTime = new Date(new Date(adjusted.showTime).getTime() + (5.5 * 60 * 60 * 1000)).toISOString();
    }
    
    if (adjusted.endTime) {
      adjusted.endTime = new Date(new Date(adjusted.endTime).getTime() + (5.5 * 60 * 60 * 1000)).toISOString();
    }
    
    if (adjusted.lockExpiryTime) {
      adjusted.lockExpiryTime = new Date(new Date(adjusted.lockExpiryTime).getTime() + (5.5 * 60 * 60 * 1000)).toISOString();
    }
    
    if (adjusted.bookingTime) {
      adjusted.bookingTime = new Date(new Date(adjusted.bookingTime).getTime() + (5.5 * 60 * 60 * 1000)).toISOString();
    }
    
    // Recursively adjust nested objects
    Object.keys(adjusted).forEach(key => {
      if (typeof adjusted[key] === 'object' && adjusted[key] !== null) {
        adjusted[key] = adjustForTimezone(adjusted[key]);
      }
    });
    
    return adjusted;
  }
};

// Create axios instance with timezone middleware
const api = axios.create({
  baseURL: API_BASE_URL,
});

api.interceptors.response.use(
  (response) => {
    // Apply timezone adjustment to response data
    response.data = adjustForTimezone(response.data);
    return response;
  },
  (error) => {
    return Promise.reject(error);
  }
);

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
  CONFIRM_BOOKING: `${API_BASE_URL}/bookings/confirm`,
  CREATE_BOOKING: `${API_BASE_URL}/bookings/create`,
  
  // Health
  HEALTH: `${API_BASE_URL}/health`
};

export default API_BASE_URL;
