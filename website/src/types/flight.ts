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
  flightIds: number[];
}

export interface FlightJourney {
  totalDuration: number;
  totalTimeInAir: number;
  price: FlightPrice;
  segments: FlightSegment[];
  layovers: Layover[];
  totalStops: number;
  flightType: 'DIRECT' | 'ONE_STOP' | 'MULTIPLE_STOPS';
}

export interface FlightSegment {
  airline: FlightAirline;
  departure: FlightStop;
  arrival: FlightStop;
  segmentDuration: number;
  price: FlightPrice;
}

export interface Layover {
  airportCode: string;
  airportName: string;
  duration: number;
  formattedLayover: string;
}

export interface FlightAirline {
  name: string;
  logoUrl: string;
}

export interface FlightStop {
  departsAt: number;
  arrivesAt: number;
  airportCode: string;
  city: string;
}

export interface FlightPrice {
  amount: number;
  currency: string;
  perPerson: boolean;
  formattedPrice: string;
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
  flightIds?: number[];
}

export interface SearchParams {
  from: string;
  to: string;
  departDate: string;
  returnDate?: string;
  passengers: number;
  class: 'economy' | 'business' | 'first';
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