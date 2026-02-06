import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const CitySelection = () => {
  const [cities, setCities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    fetchCities();
  }, []);

  const fetchCities = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/cities');
      setCities(response.data);
      setLoading(false);
    } catch (err) {
      setError('Failed to fetch cities');
      setLoading(false);
    }
  };

  const handleCitySelect = (cityId) => {
    navigate(`/movies/${cityId}`);
  };

  if (loading) {
    return (
      <div className="container">
        <div className="loading">Loading cities...</div>
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
      <h1>Select Your City</h1>
      <div className="city-grid">
        {cities.map((city) => (
          <div
            key={city.id}
            className="city-card"
            onClick={() => handleCitySelect(city.id)}
          >
            <h3>{city.name}</h3>
          </div>
        ))}
      </div>
    </div>
  );
};

export default CitySelection;
