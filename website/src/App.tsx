import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Header } from './components/Header';
import { SearchForm } from './components/SearchForm';
import { FlightCard } from './components/FlightCard';
import { BookingForm } from './components/BookingForm';
import { BookingConfirmation } from './components/BookingConfirmation';
import { AdminPanel } from './components/AdminPanel';
import { Flight, SearchParams, BookingDetails } from './types/flight';
import { searchFlights } from './services/api';

type AppView = 'search' | 'admin';
type BookingStep = 'search' | 'results' | 'booking' | 'confirmation';

function App() {
  const [currentView, setCurrentView] = useState<AppView>('search');
  const [bookingStep, setBookingStep] = useState<BookingStep>('search');
  const [flights, setFlights] = useState<Flight[]>([]);
  const [selectedFlight, setSelectedFlight] = useState<Flight | null>(null);
  const [searchParams, setSearchParams] = useState<SearchParams | null>(null);
  const [booking, setBooking] = useState<BookingDetails | null>(null);
  const [loading, setLoading] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(false);
  const [totalResults, setTotalResults] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const observer = useRef<IntersectionObserver | null>(null);
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

  const handleSearch = async (params: SearchParams) => {
    setLoading(true);
    setSearchParams(params);
    setCurrentPage(0);
    setFlights([]);
    try {
      const { flights: results, hasMore: moreAvailable, totalResults: total } = await searchFlights(params, 0, 10);
      setFlights(results);
      setHasMore(moreAvailable);
      setTotalResults(total);
      setCurrentPage(0);
      setBookingStep('results');
    } catch (error) {
      console.error('Search failed:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleFlightSelect = (flight: Flight) => {
    setSelectedFlight(flight);
    setBookingStep('booking');
  };

  const handleBookingComplete = (bookingDetails: BookingDetails) => {
    setBooking({ ...bookingDetails, bookingReference: `CR${Date.now().toString().slice(-6)}` });
    setBookingStep('confirmation');
  };

  const handleNewSearch = () => {
    setBookingStep('search');
    setFlights([]);
    setSelectedFlight(null);
    setBooking(null);
    setSearchParams(null);
    setHasMore(false);
    setTotalResults(0);
    setCurrentPage(0);
  };

  const handleBackToResults = () => {
    setBookingStep('results');
    setSelectedFlight(null);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header currentView={currentView} onViewChange={setCurrentView} />
      
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {currentView === 'admin' ? (
          <AdminPanel />
        ) : (
          <>
            {bookingStep === 'search' && (
              <div>
                <div className="text-center mb-8">
                  <h2 className="text-4xl font-bold text-gray-900 mb-4">
                    Find Your Perfect Flight
                  </h2>
                  <p className="text-xl text-gray-600">
                    Search and book flights with confidence on Credair
                  </p>
                </div>
                <SearchForm onSearch={handleSearch} loading={loading} />
              </div>
            )}

            {bookingStep === 'results' && (
              <div>
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
              </div>
            )}

            {bookingStep === 'booking' && selectedFlight && searchParams && (
              <BookingForm
                flight={selectedFlight}
                passengerCount={searchParams.passengers}
                onBookingComplete={handleBookingComplete}
                onBack={handleBackToResults}
              />
            )}

            {bookingStep === 'confirmation' && booking && (
              <BookingConfirmation
                booking={booking}
                onNewSearch={handleNewSearch}
              />
            )}
          </>
        )}
      </main>
    </div>
  );
}

export default App;