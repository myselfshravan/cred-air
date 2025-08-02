import { Flight, SearchParams, BookingDetails, PaymentDetails, FlightSearchResponse, FlightSearchResult } from '../types/flight';

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

export const bookFlight = async (bookingDetails: BookingDetails): Promise<{ success: boolean; bookingReference: string }> => {
  // Simulate booking API call
  await new Promise(resolve => setTimeout(resolve, 1500));

  const bookingReference = `CR${Date.now().toString().slice(-6)}`;
  return { success: true, bookingReference };
};

export const processPayment = async (paymentDetails: PaymentDetails, amount: number): Promise<{ success: boolean; transactionId: string }> => {
  // Simulate payment API call
  await new Promise(resolve => setTimeout(resolve, 2000));
  
  // Mock payment validation
  if (paymentDetails.cardNumber.length < 16) {
    throw new Error('Invalid card number');
  }
  
  console.log('Processing payment for amount:', amount);
  
  const transactionId = `TXN${Date.now().toString().slice(-8)}`;
  return { success: true, transactionId };
};

export const getFlightDetails = async (flightIds: number[]): Promise<Flight> => {
  try {
    const url = `http://0.0.0.0:8084/getDetails`;
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ flightIds }),
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const flightDetails: Flight = await response.json();
    return flightDetails;
  } catch (error) {
    console.error('Error fetching flight details:', error);
    throw error;
  }
};

export const uploadFlightSheet = async (file: File): Promise<{ success: boolean; processed: number }> => {
  // Simulate file processing
  await new Promise(resolve => setTimeout(resolve, 3000));
  
  return { success: true, processed: Math.floor(Math.random() * 100) + 50 };
};