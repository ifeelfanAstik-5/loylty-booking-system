import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import CitySelection from './pages/CitySelection';
import MoviesList from './pages/MoviesList';
import CinemaSelection from './pages/CinemaSelection';
import SeatSelection from './pages/SeatSelection';
import Payment from './pages/Payment';
import BookingConfirmation from './pages/BookingConfirmation';
import './App.css';

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<CitySelection />} />
          <Route path="/movies/:cityId" element={<MoviesList />} />
          <Route path="/cinemas/:cityId/:movieId" element={<CinemaSelection />} />
          <Route path="/seats/:showId" element={<SeatSelection />} />
          <Route path="/payment" element={<Payment />} />
          <Route path="/confirmation" element={<BookingConfirmation />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
