import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { API_ENDPOINTS } from '../config/api';

const MoviesList = () => {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { cityId } = useParams();
  const navigate = useNavigate();

  useEffect(() => {
    fetchMovies();
  }, [cityId]);

  const fetchMovies = async () => {
    try {
      const response = await axios.get(API_ENDPOINTS.MOVIES_BY_CITY(cityId));
      setMovies(response.data);
      setLoading(false);
    } catch (err) {
      setError('Failed to fetch movies');
      setLoading(false);
    }
  };

  const handleMovieSelect = (movieId) => {
    navigate(`/cinemas/${cityId}/${movieId}`);
  };

  const formatDuration = (minutes) => {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}h ${mins}m`;
  };

  if (loading) {
    return (
      <div className="container">
        <div className="loading">Loading movies...</div>
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
      <button className="back-button" onClick={() => navigate('/')}>
        ← Back to Cities
      </button>
      <h1>Now Showing</h1>
      <div className="movies-grid">
        {movies.map((movie) => (
          <div
            key={movie.id}
            className="movie-card"
            onClick={() => handleMovieSelect(movie.id)}
          >
            <div className="movie-poster">
              <div className="poster-placeholder">🎬</div>
            </div>
            <div className="movie-info">
              <h3>{movie.title}</h3>
              <p className="movie-genre">{movie.genre}</p>
              <p className="movie-language">{movie.language}</p>
              <p className="movie-duration">{formatDuration(movie.durationMinutes)}</p>
              <p className="movie-rating">Rating: {movie.rating}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default MoviesList;
