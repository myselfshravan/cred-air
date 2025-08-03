import {useEffect, useRef, useState} from 'react';
import {useNavigate, useSearchParams} from 'react-router-dom';
import {FlightDetailPage} from '../components/FlightDetailPage';
import {FlightJourney, Passenger} from '../types/flight';
import {getFlightJourney} from '../services/api';

export function FlightDetailsScreen() {
  const [flightJourney, setFlightJourney] = useState<FlightJourney | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const [urlSearchParams] = useSearchParams();
  const lastFlightIds = useRef<string>('');

  useEffect(() => {
    const flightIds = urlSearchParams.get('flightIds');

    if (flightIds) {
      // Prevent duplicate API calls for the same flight IDs
      if (lastFlightIds.current === flightIds) {
        return;
      }
      
      lastFlightIds.current = flightIds;
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
      const detailedFlight = await getFlightJourney(flightIdsList);
      setFlightJourney(detailedFlight);
    } catch (error) {
      console.error('Failed to fetch flight details:', error);
      setError('Failed to load flight details. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleBackToResults = () => {
    navigate(-1); // Go back to previous page
  };

  const handleContinueToPayment = (passengerList: Passenger[]) => {
    const flightIds = urlSearchParams.get('flightIds');
    const paymentParams = new URLSearchParams();
    if (flightIds) {
      paymentParams.set('flightIds', flightIds);
    }
    paymentParams.set('passengerData', JSON.stringify(passengerList));
    navigate(`/payment?${paymentParams.toString()}`);
  };

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          <span className="ml-3 text-gray-600">Loading flight details...</span>
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

  if (!flightJourney || !flightJourney) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="text-center py-12">
          <p className="text-gray-500 mb-4">Flight details not found.</p>
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
    <FlightDetailPage
      flightJourney={flightJourney}
      onBack={handleBackToResults}
      onContinueToPayment={handleContinueToPayment}
    />
  );
}