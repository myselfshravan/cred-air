import {useEffect, useRef, useState} from 'react';
import {useNavigate, useSearchParams} from 'react-router-dom';
import {FlightDetailPage} from '../components/FlightDetailPage';
import {BookingRequestPayload, FlightJourney, PassengerData, SearchParams} from '../types/flight';
import {getFlightJourney, bookFlight} from '../services/api';

export function FlightDetailsScreen() {
  const [flightJourney, setFlightJourney] = useState<FlightJourney | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searchParams, setSearchParams] = useState<SearchParams | null>(null);
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
      
      // Extract search parameters from URL
      const from = urlSearchParams.get('from');
      const to = urlSearchParams.get('to');
      const departDate = urlSearchParams.get('departDate');
      const returnDate = urlSearchParams.get('returnDate');
      const passengers = urlSearchParams.get('passengers');
      const flightClass = urlSearchParams.get('class');

      if (from && to && departDate && passengers && flightClass) {
        const params: SearchParams = {
          from,
          to,
          departDate,
          returnDate: returnDate || undefined,
          passengers: parseInt(passengers),
          class: flightClass as 'economy' | 'business' | 'first'
        };
        setSearchParams(params);
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

  const handleContinueToPayment = async (passengerList: PassengerData[]) => {
    if (!flightJourney) return;
    
    setLoading(true);
    setError(null);
    
    try {
      const bookingDetails: BookingRequestPayload = {
        flightIds: flightJourney.segments.map(segment => segment.id.toString()),
        passengerData: passengerList.map(passenger => ({
          title: passenger.title,
          firstName: passenger.firstName,
          lastName: passenger.lastName,
          dateOfBirth: passenger.dateOfBirth,
          email: passenger.email,
          phone: passenger.phone,
          id: passenger.id ?? ''
        })),
        flightPrices: flightJourney.segments.map(segment => ({
          flightId: segment.id.toString(),
          price: segment.price.amount,
          currency: segment.price.currency
        })),
        totalPrice: flightJourney.price.amount,
        passengerCount: passengerList.length
      }
      
      const result = await bookFlight(bookingDetails);
      
      if (result.success) {
        // Navigate to success page or show success message
        navigate(`/booking-success?reference=${result.bookingReference}`);
      }
    } catch (error) {
      console.error('Booking failed:', error);
      setError('Booking failed. Please try again.');
    } finally {
      setLoading(false);
    }
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
      searchParams={searchParams ?? undefined}
      onBack={handleBackToResults}
      onContinueToPayment={handleContinueToPayment}
    />
  );
}