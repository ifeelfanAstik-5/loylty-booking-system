import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const BookingConfirmation = () => {
  const [bookingData, setBookingData] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const storedData = sessionStorage.getItem('bookingConfirmation');
    if (!storedData) {
      navigate('/');
      return;
    }

    const data = JSON.parse(storedData);
    setBookingData(data);
    
    // Clear session storage after displaying confirmation
    sessionStorage.removeItem('bookingConfirmation');
  }, [navigate]);

  const handleNewBooking = () => {
    navigate('/');
  };

  const formatDateTime = (dateTimeString) => {
    const date = new Date(dateTimeString);
    return date.toLocaleString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getRowLabel = (rowNumber) => {
    return String.fromCharCode(65 + rowNumber - 1);
  };

  if (!bookingData) {
    return (
      <div className="container">
        <div className="loading">Loading booking confirmation...</div>
      </div>
    );
  }

  return (
    <div className="container">
      <div className="confirmation-container">
        <div className="success-header">
          <div className="success-icon">✓</div>
          <h1>Booking Confirmed!</h1>
          <p>Your tickets have been successfully booked.</p>
        </div>

        <div className="booking-details">
          <div className="detail-section">
            <h3>Booking Information</h3>
            <div className="detail-row">
              <span>Booking ID:</span>
              <span className="booking-id">#{bookingData.bookingId}</span>
            </div>
            <div className="detail-row">
              <span>Guest Name:</span>
              <span>{bookingData.guestName}</span>
            </div>
            <div className="detail-row">
              <span>Email:</span>
              <span>{bookingData.guestEmail}</span>
            </div>
            <div className="detail-row">
              <span>Booking Time:</span>
              <span>{formatDateTime(bookingData.bookingTime)}</span>
            </div>
            <div className="detail-row">
              <span>Status:</span>
              <span className="status confirmed">{bookingData.status}</span>
            </div>
          </div>

          <div className="detail-section">
            <h3>Movie Details</h3>
            <div className="detail-row">
              <span>Movie:</span>
              <span className="movie-title">{bookingData.movieTitle}</span>
            </div>
            <div className="detail-row">
              <span>Cinema:</span>
              <span>{bookingData.cinemaName}</span>
            </div>
            <div className="detail-row">
              <span>Screen:</span>
              <span>{bookingData.screenName}</span>
            </div>
            <div className="detail-row">
              <span>Show Time:</span>
              <span>{formatDateTime(bookingData.showTime)}</span>
            </div>
          </div>

          <div className="detail-section">
            <h3>Seat Information</h3>
            <div className="seats-grid">
              {bookingData.seats.map((seat, index) => (
                <div key={index} className="seat-ticket">
                  <div className="seat-number">
                    {getRowLabel(seat.rowNumber)}{seat.seatNumber}
                  </div>
                  <div className="seat-category">
                    {seat.category}
                    {seat.category === 'PREMIUM' && ' ⭐'}
                  </div>
                </div>
              ))}
            </div>
            <div className="total-seats">
              Total Seats: {bookingData.seats.length}
            </div>
          </div>

          <div className="detail-section">
            <h3>Payment Summary</h3>
            <div className="price-breakdown">
              {bookingData.seats.filter(seat => seat.category === 'REGULAR').length > 0 && (
                <div className="price-row">
                  <span>Regular ({bookingData.seats.filter(seat => seat.category === 'REGULAR').length} seats):</span>
                  <span>₹{bookingData.seats.filter(seat => seat.category === 'REGULAR').reduce((sum, seat) => sum + 250, 0)}</span>
                </div>
              )}
              {bookingData.seats.filter(seat => seat.category === 'PREMIUM').length > 0 && (
                <div className="price-row">
                  <span>Premium ({bookingData.seats.filter(seat => seat.category === 'PREMIUM').length} seats):</span>
                  <span>₹{bookingData.seats.filter(seat => seat.category === 'PREMIUM').reduce((sum, seat) => sum + 350, 0)}</span>
                </div>
              )}
              <div className="price-row total">
                <span>Total Amount Paid:</span>
                <span className="total-amount">₹{bookingData.totalAmount}</span>
              </div>
            </div>
          </div>
        </div>

        <div className="confirmation-actions">
          <button className="new-booking-button" onClick={handleNewBooking}>
            Book Another Movie
          </button>
        </div>

        <div className="important-notes">
          <h4>Important Information:</h4>
          <ul>
            <li>Please arrive at the cinema 30 minutes before the show time</li>
            <li>Carry a valid ID proof for verification</li>
            <li>Booking confirmation has been sent to your email</li>
            <li>No refunds or cancellations after booking confirmation</li>
            <li>Outside food and beverages are not allowed inside the cinema</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default BookingConfirmation;
