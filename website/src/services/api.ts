import {
  BookingRequestPayload,
  Flight,
  FlightJourney,
  FlightSearchResponse,
  FlightSearchResult,
  SearchParams
} from '../types/flight';

const convertToFlight = (result: FlightSearchResult): Flight => {
  const departureDate = new Date(result.departureTime);
  const arrivalDate = new Date(result.arrivalTime);
  
  const formatTime = (date: Date) => {
    return date.toLocaleTimeString('en-US', { 
      hour: '2-digit', 
      minute: '2-digit',
      hour12: false 
    });
  };
  
  const formatDate = (date: Date) => {
    return date.toISOString().split('T')[0];
  };
  
  const formatDuration = (minutes: number) => {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}h ${mins}m`;
  };

  return {
    id: result.flightIds.join('-'),
    airline: result.airlineName,
    flightNumber: `${result.airlineName.substring(0, 2).toUpperCase()}${Math.floor(Math.random() * 900) + 100}`,
    departure: {
      airport: { 
        code: result.departureAirport, 
        name: `${result.departureAirport} Airport`, 
        city: result.departureAirport, 
        country: 'India' 
      },
      time: formatTime(departureDate),
      date: formatDate(departureDate)
    },
    arrival: {
      airport: { 
        code: result.arrivalAirport, 
        name: `${result.arrivalAirport} Airport`, 
        city: result.arrivalAirport, 
        country: 'India' 
      },
      time: formatTime(arrivalDate),
      date: formatDate(arrivalDate)
    },
    duration: formatDuration(result.totalDurationMinutes),
    price: result.priceAmount,
    currency: result.priceCurrency,
    availableSeats: Math.floor(Math.random() * 50) + 10,
    aircraft: 'Aircraft Info',
    stops: result.stopCount,
    stopDetails: result.stopAirports.map(airport => ({
      airport: { 
        code: airport, 
        name: `${airport} Airport`, 
        city: airport, 
        country: 'India' 
      },
      arrivalTime: '',
      departureTime: '',
      layoverDuration: ''
    })),
    airlineLogoUrl: result.airlineLogoUrl,
    flightIds: result.flightIds
  };
};

export const searchFlights = async (params: SearchParams, page: number = 0, pageSize: number = 10): Promise<{ flights: Flight[], hasMore: boolean, totalResults: number }> => {
  try {
    const url = `http://0.0.0.0:8084/search/flights?from=${params.from}&to=${params.to}&date=${params.departDate}&passengers=${params.passengers}&page=${page}&pageSize=${pageSize}`;
    
    const response = await fetch(url);
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data: FlightSearchResponse = await response.json();
    
    const flights = data.results.map((result) => convertToFlight(result));
    
    return {
      flights,
      hasMore: data.hasMore,
      totalResults: data.totalResults
    };
  } catch (error) {
    console.error('Error searching flights:', error);
    return {
      flights: [],
      hasMore: false,
      totalResults: 0
    };
  }
};

export const bookFlight = async (payload: BookingRequestPayload): Promise<{ success: boolean; bookingReference: string }> => {
  try {
    const response = await fetch('http://0.0.0.0:8080/bookings', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const result = await response.json();
    const bookingReference = result.bookingReference || `CR${Date.now().toString().slice(-6)}`;
    
    return { success: true, bookingReference };
  } catch (error) {
    console.error('Booking API call failed:', error);
    throw new Error('Booking failed. Please try again.');
  }
};
export const getFlightJourney = async (flightIds: string[]): Promise<FlightJourney> => {
  try {
    const url = `http://0.0.0.0:8084/search/getDetails`;
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(flightIds),
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const flightJourney: FlightJourney = await response.json();
    return flightJourney;
  } catch (error) {
    console.error('Error fetching flight journey:', error);
    throw error;
  }
};

export const uploadFlightSheet = async (_file: File): Promise<{ success: boolean; processed: number }> => {
  // Simulate file processing
  await new Promise(resolve => setTimeout(resolve, 3000));
  
  return { success: true, processed: Math.floor(Math.random() * 100) + 50 };
};