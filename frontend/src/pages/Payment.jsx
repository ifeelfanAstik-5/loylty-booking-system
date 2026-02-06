import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const Payment = () => {
  const [bookingData, setBookingData] = useState(null);
  const [guestInfo, setGuestInfo] = useState({ name: '', email: '' });
  const [timeLeft, setTimeLeft] = useState(300); // 5 minutes in seconds
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
    const expiryTime = new Date(data.lockInfo.lockExpiryTime);
    const currentTime = new Date();
    const timeLeftSeconds = Math.max(0, Math.floor((expiryTime - currentTime) / 1000));
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

  const handleTimeout = () => {
    alert('Your session has expired. Please select seats again.');
    sessionStorage.removeItem('bookingData');
    navigate('/');
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
      // Create booking
      const bookingResponse = await axios.post('http://localhost:8080/api/bookings/create', {
        showId: bookingData.showId,
        guestName: guestInfo.name,
        guestEmail: guestInfo.email,
        seatIds: bookingData.selectedSeats.map(seat => seat.id),
        userId: bookingData.lockInfo.lockUserId
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
