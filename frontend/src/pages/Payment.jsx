import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { API_ENDPOINTS } from '../config/api';

const Payment = () => {
  const [bookingData, setBookingData] = useState(null);
  const [guestInfo, setGuestInfo] = useState({ name: '', email: '' });
  const [timeLeft, setTimeLeft] = useState(300); // Back to 300 seconds
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const storedData = sessionStorage.getItem('bookingData');
    if (!storedData) {
      navigate('/');
      return;
    }

    const data = JSON.parse(storedData);
    setBookingData(data);
    
    // Calculate time left based on lock expiry time
    // The backend returns UTC time, so we need to handle it properly
    const expiryTime = new Date(data.lockInfo.lockExpiryTime);
    const currentTime = new Date();
    
    // Add 5 hours to account for UTC to IST conversion
    // This is a temporary fix - the real solution is to fix the backend timezone
    const adjustedExpiryTime = new Date(expiryTime.getTime() + (5.5 * 60 * 60 * 1000));
    
    const timeLeftSeconds = Math.max(0, Math.floor((adjustedExpiryTime - currentTime) / 1000));
    
    setTimeLeft(timeLeftSeconds);
  }, [navigate]);

  useEffect(() => {
    if (timeLeft <= 0) {
      handleTimeout();
      return;
    }

    const timer = setInterval(() => {
      setTimeLeft(prev => {
        if (prev <= 1) {
          handleTimeout();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [timeLeft]);

  // Refresh seat status periodically to get real-time updates
  useEffect(() => {
    if (!bookingData || !bookingData.showId) return;
    
    const refreshInterval = setInterval(async () => {
      try {
        const response = await axios.get(API_ENDPOINTS.SEAT_LAYOUT(bookingData.showId));
        setBookingData(prev => ({ ...prev, seats: response.data }));
        
        // Check if our locked seats are still locked
        const lockedSeats = response.data.filter(seat => 
          bookingData.selectedSeats.some(selected => selected.id === seat.id) && 
          seat.status === 'LOCKED' && 
          seat.lockUserId === bookingData.lockInfo.lockUserId
        );
        
        // If our seats are no longer locked, the session has expired
        if (lockedSeats.length !== bookingData.selectedSeats.length) {
          setTimeLeft(0); // Just set timeLeft to 0, don't call handleTimeout directly
        }
      } catch (err) {
        console.error('Failed to refresh seat status:', err);
      }
    }, 5000); // Refresh every 5 seconds

    return () => clearInterval(refreshInterval);
  }, [bookingData]);

  const handleTimeout = () => {
    setError('Your session has expired. Please select seats again.');
    sessionStorage.removeItem('bookingData');
    setTimeout(() => {
      navigate('/');
    }, 2000); // Give user time to see the error message
  };

  const formatTime = (seconds) => {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes}:${secs.toString().padStart(2, '0')}`;
  };

  const calculateTotal = () => {
    if (!bookingData) return 0;
    
    return bookingData.selectedSeats.reduce((total, seat) => {
      const price = seat.category === 'PREMIUM' ? 
        bookingData.show.premiumPrice : bookingData.show.basePrice;
      return total + (price || 0);
    }, 0);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setGuestInfo(prev => ({ ...prev, [name]: value }));
  };

  const handlePayment = async () => {
    if (!guestInfo.name || !guestInfo.email) {
      setError('Please fill in all fields');
      return;
    }

    if (!guestInfo.email.includes('@')) {
      setError('Please enter a valid email address');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // Confirm booking (seats should already be locked)
      const bookingResponse = await axios.post(API_ENDPOINTS.CONFIRM_BOOKING, {
        showId: bookingData.showId,
        seatIds: bookingData.selectedSeats.map(seat => seat.id),
        userId: bookingData.lockInfo.lockUserId,
        guestName: guestInfo.name,
        guestEmail: guestInfo.email
      });

      // Clear session storage
      sessionStorage.removeItem('bookingData');

      // Store booking confirmation data
      sessionStorage.setItem('bookingConfirmation', JSON.stringify(bookingResponse.data));

      // Navigate to confirmation page
      navigate('/confirmation');
    } catch (err) {
      setError(err.response?.data?.message || 'Payment failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const getRowLabel = (rowNumber) => {
    return String.fromCharCode(65 + rowNumber - 1);
  };

  if (!bookingData) {
    return (
      <div className="container">
        <div className="loading">Loading payment details...</div>
      </div>
    );
  }

  return (
    <div className="container">
      <button className="back-button" onClick={() => navigate(-1)}>
        ← Back to Seat Selection
      </button>

      <div className="payment-container">
        <div className="payment-header">
          <h1>Complete Your Booking</h1>
          <div className={`timer ${timeLeft <= 60 ? 'warning' : ''}`}>
            Time Left: {formatTime(timeLeft)}
          </div>
        </div>

        <div className="payment-content">
          <div className="booking-summary">
            <h3>Booking Details</h3>
            <div className="movie-info">
              <h4>{bookingData.show.movie.title}</h4>
              <p>{bookingData.show.cinema.name} - {bookingData.show.screenName}</p>
              <p>{new Date(bookingData.show.showTime).toLocaleString()}</p>
            </div>

            <div className="seats-info">
              <h4>Selected Seats ({bookingData.selectedSeats.length})</h4>
              <div className="seat-list">
                {bookingData.selectedSeats.map(seat => (
                  <span key={seat.id} className="seat-tag">
                    {getRowLabel(seat.rowNumber)}{seat.seatNumber}
                    {seat.category === 'PREMIUM' && ' ⭐'}
                  </span>
                ))}
              </div>
            </div>

            <div className="price-breakdown">
              <div className="price-row">
                <span>Regular ({bookingData.selectedSeats.filter(s => s.category === 'REGULAR').length} seats):</span>
                <span>₹{bookingData.selectedSeats.filter(s => s.category === 'REGULAR').length * (bookingData.show.basePrice || 0)}</span>
              </div>
              <div className="price-row">
                <span>Premium ({bookingData.selectedSeats.filter(s => s.category === 'PREMIUM').length} seats):</span>
                <span>₹{bookingData.selectedSeats.filter(s => s.category === 'PREMIUM').length * (bookingData.show.premiumPrice || 0)}</span>
              </div>
              <div className="price-row total">
                <span>Total Amount:</span>
                <span>₹{calculateTotal()}</span>
              </div>
            </div>
          </div>

          <div className="payment-form">
            <h3>Guest Information</h3>
            <form onSubmit={(e) => { e.preventDefault(); handlePayment(); }}>
              <div className="form-group">
                <label htmlFor="name">Full Name *</label>
                <input
                  type="text"
                  id="name"
                  name="name"
                  value={guestInfo.name}
                  onChange={handleInputChange}
                  placeholder="Enter your full name"
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="email">Email Address *</label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={guestInfo.email}
                  onChange={handleInputChange}
                  placeholder="Enter your email address"
                  required
                />
              </div>

              {error && (
                <div className="error-message">{error}</div>
              )}

              <button
                type="submit"
                className="pay-button"
                disabled={loading || timeLeft <= 0}
              >
                {loading ? 'Processing...' : `Pay ₹${calculateTotal()}`}
              </button>
            </form>

            <div className="payment-note">
              <p><strong>Note:</strong> This is a mock payment gateway. Clicking "Pay" will complete your booking.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Payment;
