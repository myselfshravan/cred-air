import React, { useState } from 'react';
import { Search, ArrowLeftRight, Calendar, Users } from 'lucide-react';
import { SearchParams } from '../types/flight';

interface SearchFormProps {
  onSearch: (params: SearchParams) => void;
  loading: boolean;
}

export const SearchForm: React.FC<SearchFormProps> = ({ onSearch, loading }) => {
  const [searchParams, setSearchParams] = useState<SearchParams>({
    from: 'DEL',
    to: 'BLR',
    departDate: '',
    passengers: 1,
    tripType: 'oneWay'
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchParams.from && searchParams.to && searchParams.departDate) {
      onSearch(searchParams);
    }
  };

  const swapAirports = () => {
    setSearchParams(prev => ({
      ...prev,
      from: prev.to,
      to: prev.from
    }));
  };

  return (
    <div className="bg-white rounded-xl shadow-lg p-6 mb-8">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="md:col-span-2 flex items-center space-x-2">
            <div className="flex-1">
              <label className="block text-sm font-medium text-gray-700 mb-1">From</label>
              <select
                value={searchParams.from}
                onChange={(e) => setSearchParams(prev => ({ ...prev, from: e.target.value }))}
                className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="DEL">Delhi (DEL)</option>
                <option value="BLR">Bangalore (BLR)</option>
                <option value="BOM">Mumbai (BOM)</option>
                <option value="CCU">Kolkata (CCU)</option>
                <option value="MAA">Chennai (MAA)</option>
                <option value="IXZ">Port Blair (IXZ)</option>
              </select>
            </div>
            <button
              type="button"
              onClick={swapAirports}
              className="mt-6 p-2 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-all duration-200"
            >
              <ArrowLeftRight className="w-5 h-5" />
            </button>
            <div className="flex-1">
              <label className="block text-sm font-medium text-gray-700 mb-1">To</label>
              <select
                value={searchParams.to}
                onChange={(e) => setSearchParams(prev => ({ ...prev, to: e.target.value }))}
                className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="BLR">Bangalore (BLR)</option>
                <option value="DEL">Delhi (DEL)</option>
                <option value="BOM">Mumbai (BOM)</option>
                <option value="CCU">Kolkata (CCU)</option>
                <option value="MAA">Chennai (MAA)</option>
                <option value="IXZ">Port Blair (IXZ)</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Departure</label>
            <div className="relative">
              <Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
              <input
                type="date"
                value={searchParams.departDate}
                onChange={(e) => setSearchParams(prev => ({ ...prev, departDate: e.target.value }))}
                className="w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                required
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Passengers</label>
            <div className="relative">
              <Users className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
              <select
                value={searchParams.passengers}
                onChange={(e) => setSearchParams(prev => ({ ...prev, passengers: parseInt(e.target.value) }))}
                className="w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                {[1, 2, 3, 4, 5, 6].map(num => (
                  <option key={num} value={num}>{num} {num === 1 ? 'Passenger' : 'Passengers'}</option>
                ))}
              </select>
            </div>
          </div>
        </div>

        <div className="flex justify-center pt-4">
          <button
            type="submit"
            disabled={loading}
            className="bg-gradient-to-r from-blue-600 to-teal-600 text-white px-8 py-3 rounded-lg font-semibold hover:from-blue-700 hover:to-teal-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center space-x-2"
          >
            <Search className="w-5 h-5" />
            <span>{loading ? 'Searching...' : 'Search Flights'}</span>
          </button>
        </div>
      </form>
    </div>
  );
};