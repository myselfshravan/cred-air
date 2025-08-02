import { Flight, SearchParams, BookingDetails, PaymentDetails, FlightSearchResponse, FlightSearchResult } from '../types/flight';

// Mock flight data
const mockFlights: Flight[] = [
  {
    id: '1',
    airline: 'Credair Express',
    flightNumber: 'CE101',
    departure: {
      airport: { code: 'NYC', name: 'John F. Kennedy International Airport', city: 'New York', country: 'USA' },
      time: '08:00',
      date: '2024-02-15'
    },
    arrival: {
      airport: { code: 'LAX', name: 'Los Angeles International Airport', city: 'Los Angeles', country: 'USA' },
      time: '11:30',
      date: '2024-02-15'
    },
    duration: '5h 30m',
    price: 299,
    currency: 'USD',
    availableSeats: 42,
    aircraft: 'Boeing 737-800',
    stops: 0
  },
  {
    id: '2',
    airline: 'Credair Premium',
    flightNumber: 'CP205',
    departure: {
      airport: { code: 'NYC', name: 'John F. Kennedy International Airport', city: 'New York', country: 'USA' },
      time: '14:15',
      date: '2024-02-15'
    },
    arrival: {
      airport: { code: 'LAX', name: 'Los Angeles International Airport', city: 'Los Angeles', country: 'USA' },
      time: '17:45',
      date: '2024-02-15'
    },
    duration: '5h 30m',
    price: 449,
    currency: 'USD',
    availableSeats: 28,
    aircraft: 'Airbus A320',
    stops: 0
  },
  {
    id: '3',
    airline: 'Credair Connect',
    flightNumber: 'CC308',
    departure: {
      airport: { code: 'NYC', name: 'John F. Kennedy International Airport', city: 'New York', country: 'USA' },
      time: '19:20',
      date: '2024-02-15'
    },
    arrival: {
      airport: { code: 'LAX', name: 'Los Angeles International Airport', city: 'Los Angeles', country: 'USA' },
      time: '23:55',
      date: '2024-02-15'
    },
    duration: '6h 35m',
    price: 199,
    currency: 'USD',
    availableSeats: 15,
    aircraft: 'Boeing 737-700',
    stops: 1,
    stopDetails: [{
      airport: { code: 'DEN', name: 'Denver International Airport', city: 'Denver', country: 'USA' },
      arrivalTime: '21:45',
      departureTime: '22:30',
      layoverDuration: '45m'
    }]
  },
  {
    id: '4',
    airline: 'Credair Economy',
    flightNumber: 'CE412',
    departure: {
      airport: { code: 'NYC', name: 'John F. Kennedy International Airport', city: 'New York', country: 'USA' },
      time: '06:30',
      date: '2024-02-15'
    },
    arrival: {
      airport: { code: 'LAX', name: 'Los Angeles International Airport', city: 'Los Angeles', country: 'USA' },
      time: '12:15',
      date: '2024-02-15'
    },
    duration: '7h 45m',
    price: 159,
    currency: 'USD',
    availableSeats: 8,
    aircraft: 'Boeing 737-800',
    stops: 1,
    stopDetails: [{
      airport: { code: 'CHI', name: 'Chicago O\'Hare International Airport', city: 'Chicago', country: 'USA' },
      arrivalTime: '08:45',
      departureTime: '10:30',
      layoverDuration: '1h 45m'
    }]
  }
];

const convertToFlight = (result: FlightSearchResult, index: number): Flight => {
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
    id: `flight_${index}`,
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
    airlineLogoUrl: result.airlineLogoUrl
  };
};

export const searchFlights = async (params: SearchParams): Promise<Flight[]> => {
  try {
    const url = `http://0.0.0.0:8084/search/flights?from=${params.from}&to=${params.to}&date=${params.departDate}&passengers=${params.passengers}`;
    
    const response = await fetch(url);
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const data: FlightSearchResponse = await response.json();
    
    return data.results.map((result, index) => convertToFlight(result, index));
  } catch (error) {
    console.error('Error searching flights:', error);
    // Fallback to mock data in case of API failure
    return mockFlights.filter(flight => 
      flight.departure.airport.code === params.from && 
      flight.arrival.airport.code === params.to
    );
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
  
  const transactionId = `TXN${Date.now().toString().slice(-8)}`;
  return { success: true, transactionId };
};

export const uploadFlightSheet = async (file: File): Promise<{ success: boolean; processed: number }> => {
  // Simulate file processing
  await new Promise(resolve => setTimeout(resolve, 3000));
  
  // Mock processing result
  return { success: true, processed: Math.floor(Math.random() * 100) + 50 };
};