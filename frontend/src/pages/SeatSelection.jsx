import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';

const SeatSelection = () => {
  const [seats, setSeats] = useState([]);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [show, setShow] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [lockInfo, setLockInfo] = useState(null);
  const { showId } = useParams();
  const navigate = useNavigate();

  useEffect(() => {
    fetchSeatLayout();
  }, [showId]);

  const fetchSeatLayout = async () => {
    try {
      const response = await axios.get(`http://localhost:8080/api/seats/show/${showId}/layout`);
      setSeats(response.data);
      
      // Get show details
      const showResponse = await axios.get(`http://localhost:8080/api/shows/${showId}`);
      setShow(showResponse.data);
      
      setLoading(false);
    } catch (err) {
      setError('Failed to load seat layout');
      setLoading(false);
    }
  };

  const handleSeatClick = async (seat) => {
    if (seat.status === 'BOOKED' || seat.status === 'LOCKED') {
      return;
    }

    const isSelected = selectedSeats.some(s => s.id === seat.id);
    
    if (isSelected) {
      setSelectedSeats(selectedSeats.filter(s => s.id !== seat.id));
    } else {
      setSelectedSeats([...selectedSeats, seat]);
    }
  };

  const calculateTotal = () => {
    return selectedSeats.reduce((total, seat) => {
      const price = seat.category === 'PREMIUM' ? show?.premiumPrice : show?.basePrice;
      return total + (price || 0);
    }, 0);
  };

  const handleProceedToPayment = async () => {
    if (selectedSeats.length === 0) {
      alert('Please select at least one seat');
      return;
    }

    try {
      const lockResponse = await axios.post('http://localhost:8080/api/bookings/lock-seats', {
        showId: parseInt(showId),
        seatIds: selectedSeats.map(seat => seat.id)
      });

      if (lockResponse.data.success) {
        setLockInfo(lockResponse.data);
        // Store booking data in sessionStorage for payment page
        sessionStorage.setItem('bookingData', JSON.stringify({
          showId,
          selectedSeats,
          lockInfo: lockResponse.data,
          show
        }));
        navigate('/payment');
      } else {
        alert('Failed to lock seats: ' + lockResponse.data.message);
      }
    } catch (err) {
      alert('Error locking seats: ' + err.message);
    }
  };

  const getSeatClassName = (seat) => {
    let className = 'seat';
    if (seat.status === 'BOOKED') className += ' booked';
    else if (seat.status === 'LOCKED') className += ' locked';
    else if (selectedSeats.some(s => s.id === seat.id)) className += ' selected';
    else if (seat.category === 'PREMIUM') className += ' premium';
    else className += ' available';
    
    return className;
  };

  const getRowLabel = (rowNumber) => {
    return String.fromCharCode(65 + rowNumber - 1); // A, B, C, etc.
  };

  const groupSeatsByRow = () => {
    const rows = {};
    seats.forEach(seat => {
      if (!rows[seat.rowNumber]) {
        rows[seat.rowNumber] = [];
      }
      rows[seat.rowNumber].push(seat);
    });
    return rows;
  };

  if (loading) {
    return (
      <div className="container">
        <div className="loading">Loading seat layout...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container">
        <div className="error">{error}</div>
      </div>
    );
  }

  const rows = groupSeatsByRow();

  return (
    <div className="container">
      <button className="back-button" onClick={() => navigate(-1)}>
        ← Back to Showtimes
      </button>
      
      <div className="show-info">
        <h1>{show?.movie?.title}</h1>
        <p>{show?.cinema?.name} - {show?.screenName}</p>
        <p>{new Date(show?.showTime).toLocaleString()}</p>
      </div>

      <div className="seat-legend">
        <div className="legend-item">
          <div className="seat available"></div>
          <span>Available</span>
        </div>
        <div className="legend-item">
          <div className="seat selected"></div>
          <span>Selected</span>
        </div>
        <div className="legend-item">
          <div className="seat locked"></div>
          <span>Locked</span>
        </div>
        <div className="legend-item">
          <div className="seat booked"></div>
          <span>Booked</span>
        </div>
        <div className="legend-item">
          <div className="seat premium"></div>
          <span>Premium</span>
        </div>
      </div>

      <div className="screen">SCREEN</div>

      <div className="seat-layout">
        {Object.entries(rows).map(([rowNumber, rowSeats]) => (
          <div key={rowNumber} className="seat-row">
            <div className="row-label">{getRowLabel(parseInt(rowNumber))}</div>
            {rowSeats.map((seat) => (
              <div
                key={seat.id}
                className={getSeatClassName(seat)}
                onClick={() => handleSeatClick(seat)}
                title={`Row ${getRowLabel(seat.rowNumber)}, Seat ${seat.seatNumber} (${seat.category})`}
              >
                {seat.seatNumber}
              </div>
            ))}
            <div className="row-label">{getRowLabel(parseInt(rowNumber))}</div>
          </div>
        ))}
      </div>

      <div className="booking-summary">
        <div className="selected-seats">
          <h3>Selected Seats ({selectedSeats.length})</h3>
          {selectedSeats.map(seat => (
            <span key={seat.id} className="seat-tag">
              {getRowLabel(seat.rowNumber)}{seat.seatNumber}
            </span>
          ))}
        </div>
        
        <div className="pricing">
          <div className="price-breakdown">
            {selectedSeats.filter(s => s.category === 'REGULAR').length > 0 && (
              <div>
                Regular ({selectedSeats.filter(s => s.category === 'REGULAR').length} × ₹{show?.basePrice}): 
                ₹{selectedSeats.filter(s => s.category === 'REGULAR').length * (show?.basePrice || 0)}
              </div>
            )}
            {selectedSeats.filter(s => s.category === 'PREMIUM').length > 0 && (
              <div>
                Premium ({selectedSeats.filter(s => s.category === 'PREMIUM').length} × ₹{show?.premiumPrice}): 
                ₹{selectedSeats.filter(s => s.category === 'PREMIUM').length * (show?.premiumPrice || 0)}
              </div>
            )}
          </div>
          <div className="total">
            Total: ₹{calculateTotal()}
          </div>
        </div>

        <button 
          className="proceed-button"
          onClick={handleProceedToPayment}
          disabled={selectedSeats.length === 0}
        >
          Proceed to Payment
        </button>
      </div>
    </div>
  );
};

export default SeatSelection;
