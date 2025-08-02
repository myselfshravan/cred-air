export interface Airport {
  code: string;
  name: string;
  city: string;
  country: string;
}

export interface FlightSearchResult {
  airlineName: string;
  airlineLogoUrl: string;
  departureTime: number;
  arrivalTime: number;
  departureAirport: string;
  arrivalAirport: string;
  totalDurationMinutes: number;
  stopCount: number;
  stopAirports: string[];
  priceAmount: number;
  priceCurrency: string;
}

export interface FlightSearchResponse {
  results: FlightSearchResult[];
  totalResults: number;
  nextStartIndex: number;
  hasMore: boolean;
  pageSize: number;
}

export interface Flight {
  id: string;
  airline: string;
  flightNumber: string;
  departure: {
    airport: Airport;
    time: string;
    date: string;
  };
  arrival: {
    airport: Airport;
    time: string;
    date: string;
  };
  duration: string;
  price: number;
  currency: string;
  availableSeats: number;
  aircraft: string;
  stops: number;
  stopDetails?: {
    airport: Airport;
    arrivalTime: string;
    departureTime: string;
    layoverDuration: string;
  }[];
  airlineLogoUrl?: string;
}

export interface SearchParams {
  from: string;
  to: string;
  departDate: string;
  passengers: number;
  tripType: 'oneWay' | 'roundTrip';
}

export interface Passenger {
  id: string;
  title: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  email: string;
  phone: string;
}

export interface BookingDetails {
  flight: Flight;
  passengers: Passenger[];
  totalPrice: number;
  bookingReference?: string;
}

export interface PaymentDetails {
  cardNumber: string;
  expiryMonth: string;
  expiryYear: string;
  cvv: string;
  cardholderName: string;
}