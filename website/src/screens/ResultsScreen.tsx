import React, { useState, useCallback, useRef, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { FlightCard } from '../components/FlightCard';
import { Flight, SearchParams } from '../types/flight';
import { searchFlights } from '../services/api';

export function ResultsScreen() {
  const [flights, setFlights] = useState<Flight[]>([]);
  const [loading, setLoading] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(false);
  const [totalResults, setTotalResults] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [loadingFlightDetails, setLoadingFlightDetails] = useState(false);
  const [searchParams, setSearchParams] = useState<SearchParams | null>(null);
  const observer = useRef<IntersectionObserver | null>(null);
  const navigate = useNavigate();
  const [urlSearchParams] = useSearchParams();

  useEffect(() => {
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
      performSearch(params);
    } else {
      navigate('/');
    }
  }, [urlSearchParams, navigate]);

  const performSearch = async (params: SearchParams) => {
    setLoading(true);
    setCurrentPage(0);
    setFlights([]);
    try {
      const { flights: results, hasMore: moreAvailable, totalResults: total } = await searchFlights(params, 0, 10);
      setFlights(results);
      setHasMore(moreAvailable);
      setTotalResults(total);
      setCurrentPage(0);
    } catch (error) {
      console.error('Search failed:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadMoreFlights = useCallback(async () => {
    if (!searchParams || loadingMore || !hasMore) return;
    
    setLoadingMore(true);
    try {
      const nextPage = currentPage + 1;
      const { flights: moreResults, hasMore: moreAvailable } = await searchFlights(searchParams, nextPage, 10);
      setFlights(prev => [...prev, ...moreResults]);
      setHasMore(moreAvailable);
      setCurrentPage(nextPage);
    } catch (error) {
      console.error('Failed to load more flights:', error);
    } finally {
      setLoadingMore(false);
    }
  }, [searchParams, loadingMore, hasMore, currentPage]);

  const lastFlightElementRef = useCallback((node: HTMLDivElement) => {
    if (loading || loadingMore) return;
    if (observer.current) observer.current.disconnect();
    observer.current = new IntersectionObserver(entries => {
      if (entries[0].isIntersecting && hasMore) {
        loadMoreFlights();
      }
    });
    if (node) observer.current.observe(node);
  }, [loading, loadingMore, hasMore, loadMoreFlights]);

  const handleFlightSelect = async (flight: Flight) => {
    if (!flight.flightIds || flight.flightIds.length === 0) {
      console.error('No flight IDs available for selected flight');
      return;
    }

    setLoadingFlightDetails(true);
    try {
      navigate(`/flight-details?flightIds=${flight.flightIds.join(',')}`);
    } catch (error) {
      console.error('Failed to navigate to flight details:', error);
    } finally {
      setLoadingFlightDetails(false);
    }
  };

  const handleNewSearch = () => {
    navigate('/');
  };

  if (loading) {
    return (
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          <span className="ml-3 text-gray-600">Searching for flights...</span>
        </div>
      </main>
    );
  }

  return (
    <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Available Flights</h2>
          <p className="text-gray-600">
            {searchParams?.from} → {searchParams?.to} • {searchParams?.departDate} • {searchParams?.passengers} passenger{searchParams?.passengers !== 1 ? 's' : ''}
          </p>
          {totalResults > 0 && (
            <p className="text-sm text-gray-500 mt-1">
              Showing {flights.length} of {totalResults} flights
            </p>
          )}
        </div>
        <button
          onClick={handleNewSearch}
          className="text-blue-600 hover:text-blue-700 font-medium"
        >
          New Search
        </button>
      </div>
      
      <div className="space-y-4">
        {flights.map((flight, index) => (
          <div
            key={flight.id}
            ref={index === flights.length - 1 ? lastFlightElementRef : null}
          >
            <FlightCard
              flight={flight}
              onSelect={handleFlightSelect}
              loading={loadingFlightDetails}
            />
          </div>
        ))}
        
        {loadingMore && (
          <div className="flex justify-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            <span className="ml-2 text-gray-600">Loading more flights...</span>
          </div>
        )}
        
        {!hasMore && flights.length > 0 && (
          <div className="text-center py-8">
            <p className="text-gray-500">You've reached the end of the results</p>
          </div>
        )}
        
        {flights.length === 0 && !loading && (
          <div className="text-center py-12">
            <p className="text-gray-500">No flights found for your search criteria.</p>
          </div>
        )}
      </div>
    </main>
  );
}