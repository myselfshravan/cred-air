import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { BookingForm } from '../components/BookingForm';
import { Flight, SearchParams, Passenger, BookingDetails } from '../types/flight';
import { getFlightDetails } from '../services/api';

export function PaymentScreen() {
  const [selectedFlight, setSelectedFlight] = useState<Flight | null>(null);
  const [passengers, setPassengers] = useState<Passenger[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const [urlSearchParams] = useSearchParams();
  const lastFlightIds = useRef<string>('');

  useEffect(() => {
    const flightIds = urlSearchParams.get('flightIds');
    const passengersData = urlSearchParams.get('passengerData');

    if (flightIds) {
      // Prevent duplicate API calls for the same flight IDs
      if (lastFlightIds.current === flightIds) {
        return;
      }
      
      lastFlightIds.current = flightIds;

      if (passengersData) {
        try {
          const parsedPassengers = JSON.parse(passengersData);
          setPassengers(parsedPassengers);
        } catch (e) {
          console.error('Failed to parse passengers data:', e);
        }
      }

      fetchFlightDetails(flightIds.split(','));
    } else {
      navigate('/');
    }
  }, [urlSearchParams]);

  const fetchFlightDetails = async (flightIdsList: string[]) => {
    // Prevent multiple concurrent calls
    if (loading) {
      return;
    }
    
    setLoading(true);
    setError(null);
    try {
      const detailedFlight = await getFlightDetails(flightIdsList);
      setSelectedFlight(detailedFlight);
    } catch (error) {
      console.error('Failed to fetch flight details:', error);
      setError('Failed to load flight details. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleBookingComplete = (bookingDetails: BookingDetails) => {
    const booking = { ...bookingDetails, bookingReference: `CR${Date.now().toString().slice(-6)}` };
    const confirmationParams = new URLSearchParams();
    confirmationParams.set('booking', JSON.stringify(booking));
    navigate(`/confirmation?${confirmationParams.toString()}`);
  };

  const handleBackToResults = () => {
    navigate(-1); // Go back to previous page
  };

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          <span className="ml-3 text-gray-600">Loading payment details...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="text-center py-12">
          <p className="text-red-500 mb-4">{error}</p>
          <button
            onClick={handleBackToResults}
            className="text-blue-600 hover:text-blue-700 font-medium"
          >
            Back to Results
          </button>
        </div>
      </div>
    );
  }

  if (!selectedFlight) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="text-center py-12">
          <p className="text-gray-500 mb-4">Please select a flight first.</p>
          <button
            onClick={() => navigate('/')}
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
      <BookingForm
        flight={selectedFlight}
        passengerCount={passengers.length || 1}
        onBookingComplete={handleBookingComplete}
        onBack={handleBackToResults}
        prefilledPassengers={passengers}
      />
    </main>
  );
}