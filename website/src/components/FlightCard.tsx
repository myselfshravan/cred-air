import React from 'react';
import { Plane, Clock, MapPin, Circle } from 'lucide-react';
import { Flight } from '../types/flight';

interface FlightCardProps {
  flight: Flight;
  onSelect: (flight: Flight) => void;
}

export const FlightCard: React.FC<FlightCardProps> = ({ flight, onSelect }) => {
  return (
    <div className="bg-white rounded-xl shadow-md border border-gray-200 p-6 hover:shadow-lg transition-all duration-300 hover:border-blue-300">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 rounded-lg flex items-center justify-center overflow-hidden">
            {flight.airlineLogoUrl ? (
              <img 
                src={flight.airlineLogoUrl} 
                alt={`${flight.airline} logo`}
                className="w-full h-full object-contain"
                onError={(e) => {
                  // Fallback to default icon if image fails to load
                  const target = e.target as HTMLImageElement;
                  target.style.display = 'none';
                  target.nextElementSibling?.classList.remove('hidden');
                }}
              />
            ) : null}
            <div className={`w-full h-full bg-gradient-to-r from-blue-600 to-teal-600 rounded-lg flex items-center justify-center ${flight.airlineLogoUrl ? 'hidden' : ''}`}>
              <Plane className="w-5 h-5 text-white" />
            </div>
          </div>
          <div>
            <h3 className="font-semibold text-gray-900">{flight.airline}</h3>
            <p className="text-sm text-gray-500">{flight.flightNumber} • {flight.aircraft}</p>
          </div>
        </div>
        <div className="text-right">
          <p className="text-2xl font-bold text-gray-900">
            {flight.currency === 'USD' ? '$' : flight.currency + ' '}{flight.price}
          </p>
          <p className="text-sm text-gray-500">per person</p>
        </div>
      </div>

      <div className="flex items-center justify-between mb-4">
        <div className="text-left">
          <p className="text-lg font-semibold text-gray-900">{flight.departure.time}</p>
          <p className="text-sm text-gray-600">{flight.departure.airport.code}</p>
          <p className="text-xs text-gray-500">{flight.departure.airport.city}</p>
        </div>
        
        <div className="flex-1 mx-6">
          <div className="flex items-center justify-center">
            <div className="flex-1 h-px bg-gray-300"></div>
            <div className="flex items-center space-x-1 bg-gray-100 px-3 py-1 rounded-full">
              <Clock className="w-3 h-3 text-gray-500" />
              <span className="text-xs text-gray-600">{flight.duration}</span>
            </div>
            <div className="flex-1 h-px bg-gray-300"></div>
          </div>
          {flight.stops === 0 ? (
            <p className="text-xs text-green-600 text-center mt-1">Direct</p>
          ) : (
            <div className="mt-2">
              {flight.stopDetails && (
                <div className="flex items-center justify-center space-x-1 text-xs">
                  {flight.stopDetails.map((stop, index) => (
                    <div key={index} className="flex items-center space-x-1">
                      {index > 0 && <span className="text-gray-400">•••</span>}
                      <span className="text-orange-600 font-semibold">{stop.airport.city}</span>
                    </div>
                  ))}
                </div>
              )}
              <p className="text-xs text-orange-600 text-center mt-1">{flight.stops} layover{flight.stops > 1 ? 's' : ''}</p>
            </div>
          )}
        </div>

        <div className="text-right">
          <p className="text-lg font-semibold text-gray-900">{flight.arrival.time}</p>
          <p className="text-sm text-gray-600">{flight.arrival.airport.code}</p>
          <p className="text-xs text-gray-500">{flight.arrival.airport.city}</p>
        </div>
      </div>

      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4 text-sm text-gray-500">
          <div className="flex items-center space-x-1">
            <MapPin className="w-4 h-4" />
            <span>{flight.availableSeats} seats left</span>
          </div>
        </div>
        
        <button
          onClick={() => onSelect(flight)}
          className="bg-blue-600 text-white px-6 py-2 rounded-lg font-medium hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-all duration-200"
        >
          Select Flight
        </button>
      </div>
    </div>
  );
};