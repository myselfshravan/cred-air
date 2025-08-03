import React, { useState } from 'react';
import { User, Mail, Phone, Calendar, CreditCard, Lock } from 'lucide-react';
import { Flight, Passenger, BookingDetails, PaymentDetails } from '../types/flight';

interface BookingFormProps {
  flight: Flight;
  passengerCount: number;
  onBookingComplete: (booking: BookingDetails) => void;
  onBack: () => void;
  prefilledPassengers?: Passenger[];
}

export const BookingForm: React.FC<BookingFormProps> = ({
  flight,
  passengerCount,
  onBookingComplete,
  onBack,
  prefilledPassengers
}) => {
  const [step, setStep] = useState<'passengers' | 'payment'>(prefilledPassengers ? 'payment' : 'passengers');
  const [passengers, setPassengers] = useState<Passenger[]>(
    prefilledPassengers || Array.from({ length: passengerCount }, (_, i) => ({
      id: `passenger-${i}`,
      title: 'Mr',
      firstName: '',
      lastName: '',
      dateOfBirth: '',
      email: i === 0 ? '' : '',
      phone: i === 0 ? '' : ''
    }))
  );
  const [paymentDetails, setPaymentDetails] = useState<PaymentDetails>({
    cardNumber: '',
    expiryMonth: '',
    expiryYear: '',
    cvv: '',
    cardholderName: ''
  });
  const [loading, setLoading] = useState(false);

  const totalPrice = flight.price * passengerCount;

  const handlePassengerChange = (index: number, field: keyof Passenger, value: string) => {
    setPassengers(prev => prev.map((passenger, i) => 
      i === index ? { ...passenger, [field]: value } : passenger
    ));
  };

  const handlePaymentChange = (field: keyof PaymentDetails, value: string) => {
    setPaymentDetails(prev => ({ ...prev, [field]: value }));
  };

  const validatePassengers = () => {
    return passengers.every(passenger => 
      passenger.firstName && passenger.lastName && passenger.dateOfBirth &&
      (passenger.email || passengers[0].email) && (passenger.phone || passengers[0].phone)
    );
  };

  const handleSubmit = async () => {
    if (step === 'passengers') {
      if (validatePassengers()) {
        setStep('payment');
      }
      return;
    }

    setLoading(true);
    try {
      const bookingDetails: BookingDetails = {
        flight,
        passengers,
        totalPrice
      };
      
      // Simulate booking process
      await new Promise(resolve => setTimeout(resolve, 2000));
      onBookingComplete(bookingDetails);
    } catch (error) {
      console.error('Booking failed:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto">
      <div className="bg-white rounded-xl shadow-lg p-6">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold text-gray-900">Complete Your Booking</h2>
          <button
            onClick={onBack}
            className="text-blue-600 hover:text-blue-700 font-medium"
          >
            ← Back to Search
          </button>
        </div>

        {/* Progress Indicator */}
        <div className="flex items-center mb-8">
          <div className={`flex items-center justify-center w-8 h-8 rounded-full ${
            step === 'passengers' ? 'bg-blue-600 text-white' : 'bg-green-600 text-white'
          }`}>
            <User className="w-4 h-4" />
          </div>
          <div className={`flex-1 h-1 mx-4 ${
            step === 'payment' ? 'bg-green-600' : 'bg-gray-300'
          }`}></div>
          <div className={`flex items-center justify-center w-8 h-8 rounded-full ${
            step === 'payment' ? 'bg-blue-600 text-white' : 'bg-gray-300 text-gray-500'
          }`}>
            <CreditCard className="w-4 h-4" />
          </div>
        </div>

        {step === 'passengers' && (
          <div className="space-y-6">
            <h3 className="text-lg font-semibold text-gray-900">Passenger Information</h3>
            {passengers.map((passenger, index) => (
              <div key={passenger.id} className="border border-gray-200 rounded-lg p-6">
                <h4 className="text-md font-medium text-gray-900 mb-4">
                  Passenger {index + 1} {index === 0 && '(Primary Contact)'}
                </h4>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
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
                      <Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
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
                        <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
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
                        <Phone className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
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
          </div>
        )}

        {step === 'payment' && (
          <div className="space-y-6">
            <h3 className="text-lg font-semibold text-gray-900">Payment Information</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Cardholder Name</label>
                  <input
                    type="text"
                    value={paymentDetails.cardholderName}
                    onChange={(e) => handlePaymentChange('cardholderName', e.target.value)}
                    className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Card Number</label>
                  <div className="relative">
                    <CreditCard className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                    <input
                      type="text"
                      value={paymentDetails.cardNumber}
                      onChange={(e) => handlePaymentChange('cardNumber', e.target.value.replace(/\s/g, '').replace(/(.{4})/g, '$1 ').trim())}
                      placeholder="1234 5678 9012 3456"
                      className="w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      required
                    />
                  </div>
                </div>
                <div className="grid grid-cols-3 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Month</label>
                    <select
                      value={paymentDetails.expiryMonth}
                      onChange={(e) => handlePaymentChange('expiryMonth', e.target.value)}
                      className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      required
                    >
                      <option value="">MM</option>
                      {Array.from({ length: 12 }, (_, i) => (
                        <option key={i + 1} value={String(i + 1).padStart(2, '0')}>
                          {String(i + 1).padStart(2, '0')}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Year</label>
                    <select
                      value={paymentDetails.expiryYear}
                      onChange={(e) => handlePaymentChange('expiryYear', e.target.value)}
                      className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      required
                    >
                      <option value="">YYYY</option>
                      {Array.from({ length: 10 }, (_, i) => (
                        <option key={2024 + i} value={String(2024 + i)}>
                          {2024 + i}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">CVV</label>
                    <div className="relative">
                      <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                      <input
                        type="text"
                        value={paymentDetails.cvv}
                        onChange={(e) => handlePaymentChange('cvv', e.target.value)}
                        placeholder="123"
                        maxLength={4}
                        className="w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        required
                      />
                    </div>
                  </div>
                </div>
              </div>
              <div className="bg-gray-50 p-6 rounded-lg">
                <h4 className="font-semibold text-gray-900 mb-4">Booking Summary</h4>
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span>Outbound Flight:</span>
                    <span>{flight.flightNumber}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Passengers:</span>
                    <span>{passengerCount}</span>
                  </div>
                  <div className="border-t pt-2 mt-2">
                    <div className="flex justify-between font-semibold text-lg">
                      <span>Total:</span>
                      <span>${totalPrice}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        <div className="flex justify-between pt-6 border-t">
          {step === 'payment' && (
            <button
              onClick={() => setStep('passengers')}
              className="px-6 py-2 text-gray-600 hover:text-gray-800 font-medium"
            >
              ← Back
            </button>
          )}
          <div className="flex-1"></div>
          <button
            onClick={handleSubmit}
            disabled={loading || (step === 'passengers' && !validatePassengers())}
            className="bg-gradient-to-r from-blue-600 to-teal-600 text-white px-8 py-3 rounded-lg font-semibold hover:from-blue-700 hover:to-teal-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? 'Processing...' : step === 'passengers' ? 'Continue to Payment' : `Pay $${totalPrice}`}
          </button>
        </div>
      </div>
    </div>
  );
};