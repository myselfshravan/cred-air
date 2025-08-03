import React, {useState} from 'react';
import {ArrowLeft, Calendar, Mail, Phone, User, Plus, Minus} from 'lucide-react';
import {FlightJourney, PassengerData, SearchParams} from '../types/flight';
import {FlightTimelineView} from './FlightTimelineView';
import {PriceBreakdown} from './PriceBreakdown';

interface FlightDetailPageProps {
  flightJourney: FlightJourney;
  searchParams?: SearchParams;
  onBack: () => void;
  onContinueToPayment: (passengers: PassengerData[]) => void;
}

export const FlightDetailPage: React.FC<FlightDetailPageProps> = ({
  flightJourney,
  searchParams,
  onBack,
  onContinueToPayment
}) => {
  const defaultPassengers = 1;
  const requestedPassengers = searchParams?.passengers || defaultPassengers;
  const initialPassengerCount = requestedPassengers;
  
  const [currentPassengerCount, setCurrentPassengerCount] = useState(initialPassengerCount);
  const [passengers, setPassengers] = useState<PassengerData[]>(
    Array.from({ length: initialPassengerCount }, (_, i) => ({
      id: `passenger-${i}`,
      title: 'Mr',
      firstName: '',
      lastName: '',
      dateOfBirth: '',
      email: i === 0 ? '' : '',
      phone: i === 0 ? '' : ''
    }))
  );

  const handlePassengerChange = (index: number, field: keyof PassengerData, value: string) => {
    setPassengers(prev => prev.map((passenger, i) => 
      i === index ? { ...passenger, [field]: value } : passenger
    ));
  };

  const handleAddPassenger = () => {
    const newPassengerIndex = currentPassengerCount;
    const newPassenger: PassengerData = {
      id: `passenger-${newPassengerIndex}`,
      title: 'Mr',
      firstName: '',
      lastName: '',
      dateOfBirth: '',
      email: '',
      phone: ''
    };
    setPassengers(prev => [...prev, newPassenger]);
    setCurrentPassengerCount(prev => prev + 1);
  };

  const handleRemovePassenger = (indexToRemove: number) => {
    if (currentPassengerCount > 1 && indexToRemove > 0) { // Can't remove first passenger (primary contact)
      setPassengers(prev => prev.filter((_, index) => index !== indexToRemove));
      setCurrentPassengerCount(prev => prev - 1);
    }
  };

  const validatePassengers = () => {
    return passengers.every(passenger => 
      passenger.firstName && passenger.lastName && passenger.dateOfBirth &&
      (passenger.email || passengers[0].email) && (passenger.phone || passengers[0].phone)
    );
  };

  const handleContinue = () => {
    if (validatePassengers()) {
      onContinueToPayment(passengers);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div className="flex items-center space-x-4">
          <button
            onClick={onBack}
            className="flex items-center space-x-2 text-blue-600 hover:text-blue-700 font-medium"
          >
            <ArrowLeft className="w-4 h-4" />
            <span>Back to Results</span>
          </button>
          <div className="h-6 w-px bg-gray-300" />
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Flight Details</h1>
            {/*<p className="text-gray-600">*/}
            {/*  {flight.departure?.airport?.code || 'N/A'} → {flight.arrival?.airport?.code || 'N/A'} • {flight.departure?.date || 'N/A'}*/}
            {/*</p>*/}
          </div>
        </div>
      </div>

      {/* 2-Column Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-8">
        {/* Left Column - Traveller Details */}
        <div className="lg:col-span-3">
          <div className="bg-white rounded-xl shadow-lg p-6">
            <div className="flex items-center space-x-2 mb-6">
              <User className="w-5 h-5 text-blue-600" />
              <h3 className="text-xl font-semibold text-gray-900">Traveller Details</h3>
              <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-xs font-medium">
                {currentPassengerCount} {currentPassengerCount === 1 ? 'Passenger' : 'Passengers'}
              </span>
            </div>


            <div className="space-y-6">
              {passengers.map((passenger, index) => (
                <div key={passenger.id} className="border border-gray-200 rounded-lg p-4">
                  <div className="flex items-center justify-between mb-4">
                    <h4 className="text-md font-medium text-gray-900">
                      Passenger {index + 1} {index === 0 && '(Primary Contact)'}
                    </h4>
                    {index > 0 && (
                      <button
                        onClick={() => handleRemovePassenger(index)}
                        className="flex items-center justify-center w-8 h-8 rounded-full border border-gray-300 hover:border-red-400 hover:bg-red-50 transition-colors"
                        title="Remove this passenger"
                      >
                        <Minus className="w-4 h-4 text-red-600" />
                      </button>
                    )}
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Title</label>
                      <select
                        value={passenger.title}
                        onChange={(e) => handlePassengerChange(index, 'title', e.target.value)}
                        className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      >
                        <option value="Mr">Mr</option>
                        <option value="Mrs">Mrs</option>
                        <option value="Ms">Ms</option>
                        <option value="Dr">Dr</option>
                      </select>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">First Name</label>
                      <input
                        type="text"
                        value={passenger.firstName}
                        onChange={(e) => handlePassengerChange(index, 'firstName', e.target.value)}
                        className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Last Name</label>
                      <input
                        type="text"
                        value={passenger.lastName}
                        onChange={(e) => handlePassengerChange(index, 'lastName', e.target.value)}
                        className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Date of Birth</label>
                      <div className="relative">
                        <Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                        <input
                          type="date"
                          value={passenger.dateOfBirth}
                          onChange={(e) => handlePassengerChange(index, 'dateOfBirth', e.target.value)}
                          className="w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                          required
                        />
                      </div>
                    </div>
                    {(index === 0 || passenger.email) && (
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                          Email {index === 0 && '(Required)'}
                        </label>
                        <div className="relative">
                          <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                          <input
                            type="email"
                            value={passenger.email}
                            onChange={(e) => handlePassengerChange(index, 'email', e.target.value)}
                            className="w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            required={index === 0}
                          />
                        </div>
                      </div>
                    )}
                    {(index === 0 || passenger.phone) && (
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                          Phone {index === 0 && '(Required)'}
                        </label>
                        <div className="relative">
                          <Phone className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                          <input
                            type="tel"
                            value={passenger.phone}
                            onChange={(e) => handlePassengerChange(index, 'phone', e.target.value)}
                            className="w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            required={index === 0}
                          />
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              ))}
              
              {/* Add Passenger Button */}
              <button
                onClick={handleAddPassenger}
                className="w-full border-2 border-dashed border-gray-300 hover:border-blue-400 hover:bg-blue-50 rounded-lg p-6 flex items-center justify-center space-x-2 transition-colors"
              >
                <Plus className="w-5 h-5 text-blue-600" />
                <span className="text-blue-600 font-medium">Add Another Passenger</span>
              </button>
            </div>
          </div>
        </div>

        {/* Right Column - Flight Timeline + Price Breakdown */}
        <div className="lg:col-span-2 space-y-6">
          {/* Flight Timeline Section */}
          <FlightTimelineView flightJourney={flightJourney} />
          
          {/* Price Breakdown Section */}
          <PriceBreakdown 
            flightJourney={flightJourney} 
            passengerCount={currentPassengerCount}
            onContinueToPayment={handleContinue}
            disabled={!validatePassengers()}
          />
        </div>
      </div>

      {/* Bottom Action Bar */}
      <div className="mt-8 bg-white rounded-xl shadow-lg p-6">
        <div className="flex items-center justify-between">
          <div className="text-sm text-gray-600">
            Please review your details and continue to payment
          </div>
          <button
            onClick={handleContinue}
            disabled={!validatePassengers()}
            className="bg-gradient-to-r from-blue-600 to-teal-600 text-white px-8 py-3 rounded-lg font-semibold hover:from-blue-700 hover:to-teal-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Continue to Payment
          </button>
        </div>
      </div>
    </div>
  );
};