import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { BookingConfirmation } from '../components/BookingConfirmation';
import { BookingDetails } from '../types/flight';

export function ConfirmationScreen() {
  const [booking, setBooking] = useState<BookingDetails | null>(null);
  const navigate = useNavigate();
  const [urlSearchParams] = useSearchParams();

  useEffect(() => {
    const bookingData = urlSearchParams.get('booking');
    
    if (bookingData) {
      try {
        const parsedBooking = JSON.parse(bookingData);
        setBooking(parsedBooking);
      } catch (e) {
        console.error('Failed to parse booking data:', e);
        navigate('/');
      }
    } else {
      navigate('/');
    }
  }, [urlSearchParams, navigate]);

  const handleNewSearch = () => {
    navigate('/');
  };

  if (!booking) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="text-center py-12">
          <p className="text-gray-500 mb-4">No booking found.</p>
          <button
            onClick={handleNewSearch}
            className="text-blue-600 hover:text-blue-700 font-medium"
          >
            Start New Search
          </button>
        </div>
      </div>
    );
  }

  return (
    <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <BookingConfirmation
        booking={booking}
        onNewSearch={handleNewSearch}
      />
    </main>
  );
}