import React from 'react';
import { CheckCircle, Plane, Calendar, Users, Mail } from 'lucide-react';
import { BookingDetails } from '../types/flight';

interface BookingConfirmationProps {
  booking: BookingDetails;
  onNewSearch: () => void;
}

export const BookingConfirmation: React.FC<BookingConfirmationProps> = ({
  booking,
  onNewSearch
}) => {
  return (
    <div className="max-w-2xl mx-auto">
      <div className="bg-white rounded-xl shadow-lg p-8 text-center">
        <div className="flex justify-center mb-6">
          <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center">
            <CheckCircle className="w-8 h-8 text-green-600" />
          </div>
        </div>
        
        <h2 className="text-3xl font-bold text-gray-900 mb-2">Booking Confirmed!</h2>
        <p className="text-gray-600 mb-8">Your flight has been successfully booked.</p>
        
        <div className="bg-blue-50 rounded-lg p-6 mb-8">
          <h3 className="text-lg font-semibold text-blue-900 mb-4">Booking Reference</h3>
          <p className="text-2xl font-bold text-blue-700 tracking-wider">
            {booking.bookingReference}
          </p>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
          <div className="bg-gray-50 rounded-lg p-6">
            <div className="flex items-center space-x-2 mb-4">
              <Plane className="w-5 h-5 text-blue-600" />
              <h4 className="font-semibold text-gray-900">Flight Details</h4>
            </div>
            <div className="space-y-2 text-sm text-gray-600">
              <p><strong>Flight:</strong> {booking.flight.flightNumber}</p>
              <p><strong>Date:</strong> {booking.flight.departure.date}</p>
              <p><strong>Route:</strong> {booking.flight.departure.airport.code} → {booking.flight.arrival.airport.code}</p>
              <p><strong>Time:</strong> {booking.flight.departure.time} - {booking.flight.arrival.time}</p>
            </div>
          </div>
          
          <div className="bg-gray-50 rounded-lg p-6">
            <div className="flex items-center space-x-2 mb-4">
              <Users className="w-5 h-5 text-blue-600" />
              <h4 className="font-semibold text-gray-900">Passengers</h4>
            </div>
            <div className="space-y-2 text-sm text-gray-600">
              {booking.passengers.map((passenger, index) => (
                <p key={passenger.id}>
                  {passenger.title} {passenger.firstName} {passenger.lastName}
                </p>
              ))}
            </div>
          </div>
        </div>
        
        <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-8">
          <div className="flex items-center space-x-2 mb-2">
            <Mail className="w-5 h-5 text-yellow-600" />
            <p className="font-medium text-yellow-800">Confirmation Email Sent</p>
          </div>
          <p className="text-sm text-yellow-700">
            A confirmation email with your e-tickets has been sent to {booking.passengers[0].email}
          </p>
        </div>
        
        <div className="space-y-4">
          <button
            onClick={onNewSearch}
            className="bg-gradient-to-r from-blue-600 to-teal-600 text-white px-8 py-3 rounded-lg font-semibold hover:from-blue-700 hover:to-teal-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-all duration-200"
          >
            Book Another Flight
          </button>
          <p className="text-sm text-gray-500">
            Please arrive at the airport at least 2 hours before your domestic flight or 3 hours before international flights.
          </p>
        </div>
      </div>
    </div>
  );
};