import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';

const CinemaSelection = () => {
  const [shows, setShows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { cityId, movieId } = useParams();
  const navigate = useNavigate();

  useEffect(() => {
    fetchShows();
  }, [cityId, movieId]);

  const fetchShows = async () => {
    try {
      const response = await axios.get(`http://localhost:8080/api/shows/movie/${movieId}/city/${cityId}/grouped`);
      setShows(response.data);
      setLoading(false);
    } catch (err) {
      setError('Failed to fetch showtimes');
      setLoading(false);
    }
  };

  const handleShowSelect = (showId) => {
    navigate(`/seats/${showId}`);
  };

  const formatTime = (timeString) => {
    const date = new Date(timeString);
    return date.toLocaleTimeString('en-US', { 
      hour: '2-digit', 
      minute: '2-digit',
      hour12: true 
    });
  };

  const formatDate = (timeString) => {
    const date = new Date(timeString);
    return date.toLocaleDateString('en-US', { 
      weekday: 'short',
      month: 'short', 
      day: 'numeric' 
    });
  };

  if (loading) {
    return (
      <div className="container">
        <div className="loading">Loading showtimes...</div>
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

  return (
    <div className="container">
      <button className="back-button" onClick={() => navigate(`/movies/${cityId}`)}>
        ← Back to Movies
      </button>
      <h1>Select Cinema & Showtime</h1>
      
      {Object.entries(shows).map(([cinemaKey, cinemaShows]) => {
        const cinema = JSON.parse(cinemaKey);
        return (
          <div key={cinema.id} className="cinema-section">
            <div className="cinema-header">
              <h3>{cinema.name}</h3>
              <p className="cinema-address">{cinema.address}</p>
              <p className="cinema-chain">{cinema.theaterChainName}</p>
            </div>
            
            <div className="shows-grid">
              {cinemaShows.map((show) => (
                <div
                  key={show.id}
                  className="show-card"
                  onClick={() => handleShowSelect(show.id)}
                >
                  <div className="show-date">
                    {formatDate(show.showTime)}
                  </div>
                  <div className="show-time">
                    {formatTime(show.showTime)}
                  </div>
                  <div className="show-screen">
                    {show.screenName}
                  </div>
                  <div className="show-pricing">
                    <div className="price">
                      Regular: ₹{show.basePrice}
                    </div>
                    <div className="price premium">
                      Premium: ₹{show.premiumPrice}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default CinemaSelection;
