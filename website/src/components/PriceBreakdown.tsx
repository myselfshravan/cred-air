import React from 'react';
import { Receipt, Users } from 'lucide-react';
import { FlightJourney } from '../types/flight';

interface PriceBreakdownProps {
  flightJourney: FlightJourney;
  passengerCount: number;
  onContinueToPayment?: () => void;
  disabled?: boolean;
}

export const PriceBreakdown: React.FC<PriceBreakdownProps> = ({ flightJourney, passengerCount, onContinueToPayment, disabled = false }) => {
  const basePrice = flightJourney.price.amount;
  const taxes = Math.round(basePrice * 0.18); // 18% tax
  const fees = 50; // Fixed service fee
  const pricePerPerson = basePrice + taxes + fees;
  const displayPassengerCount = Math.max(passengerCount, 1); // Show minimum 1 passenger
  const totalPrice = pricePerPerson * displayPassengerCount;
  const isMinimumPricing = passengerCount === 0;

  const formatPrice = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: flightJourney.price.currency || 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount);
  };

  return (
    <div className="bg-white rounded-xl shadow-lg p-4">
      <div className="flex items-center space-x-2 mb-4">
        <Receipt className="w-5 h-5 text-blue-600" />
        <h3 className="text-lg font-semibold text-gray-900">Price Breakdown</h3>
      </div>

      <div className="space-y-4">
        {/* Per Person Breakdown */}
        <div className="bg-gray-50 rounded-lg p-3">
          <h4 className="font-medium text-gray-900 mb-3">Per Person</h4>
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-600">Base Fare</span>
              <span className="font-medium">{formatPrice(basePrice)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Taxes & Surcharges</span>
              <span className="font-medium">{formatPrice(taxes)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Service Fees</span>
              <span className="font-medium">{formatPrice(fees)}</span>
            </div>
            <div className="border-t pt-2 mt-2">
              <div className="flex justify-between">
                <span className="font-medium text-gray-900">Subtotal per person</span>
                <span className="font-semibold text-gray-900">{formatPrice(pricePerPerson)}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Total Breakdown */}
        <div className="border-t pt-4">
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center space-x-2">
              <Users className="w-4 h-4 text-gray-600" />
              <span className="text-gray-600">Passengers</span>
            </div>
            <span className="font-medium">{displayPassengerCount}</span>
          </div>
          
          {isMinimumPricing && (
            <div className="mb-3 bg-blue-50 border border-blue-200 rounded-lg p-3">
              <p className="text-xs text-blue-800">
                Prices shown for minimum 1 passenger. Update traveller details to see accurate pricing.
              </p>
            </div>
          )}
          
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-600">Subtotal ({displayPassengerCount} × {formatPrice(pricePerPerson)})</span>
              <span className="font-medium">{formatPrice(totalPrice)}</span>
            </div>
          </div>
        </div>

        {/* Grand Total */}
        <div className="bg-blue-50 rounded-lg p-4 border border-blue-200">
          <div className="flex justify-between items-center">
            <span className="text-lg font-semibold text-blue-900">Total Amount</span>
            <span className="text-2xl font-bold text-blue-900">{formatPrice(totalPrice)}</span>
          </div>
          <p className="text-xs text-blue-700 mt-1">All taxes and fees included</p>
        </div>

        {/* Price Details */}
        <div className="bg-gray-50 rounded-lg p-3">
          <h5 className="text-xs font-medium text-gray-700 mb-2">Price Details</h5>
          <div className="space-y-1 text-xs text-gray-600">
            <p>• Prices are in {flightJourney.price.currency || 'USD'}</p>
            <p>• Includes all applicable taxes</p>
            <p>• Non-refundable fare</p>
            <p>• Subject to airline terms</p>
          </div>
        </div>
      </div>

      {/* Action Button */}
      {onContinueToPayment && (
        <div className="mt-6 pt-6 border-t">
          <button 
            onClick={onContinueToPayment}
            disabled={disabled}
            className="w-full bg-gradient-to-r from-blue-600 to-teal-600 text-white py-3 px-4 rounded-lg font-semibold hover:from-blue-700 hover:to-teal-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Continue to Payment
          </button>
        </div>
      )}
    </div>
  );
};