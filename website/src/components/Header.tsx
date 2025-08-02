import React from 'react';
import { Plane, User, Settings } from 'lucide-react';

interface HeaderProps {
  currentView: 'search' | 'admin';
  onViewChange: (view: 'search' | 'admin') => void;
}

export const Header: React.FC<HeaderProps> = ({ currentView, onViewChange }) => {
  return (
    <header className="bg-white shadow-sm border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center space-x-3">
            <div className="flex items-center justify-center w-10 h-10 bg-gradient-to-r from-blue-600 to-teal-600 rounded-lg">
              <Plane className="w-6 h-6 text-white" />
            </div>
            <div>
              <h1 className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-teal-600 bg-clip-text text-transparent">
                Credair
              </h1>
              <p className="text-xs text-gray-500">Book with confidence</p>
            </div>
          </div>
          
          <nav className="flex items-center space-x-4">
            <button
              onClick={() => onViewChange('search')}
              className={`px-4 py-2 rounded-lg font-medium transition-all duration-200 ${
                currentView === 'search'
                  ? 'bg-blue-100 text-blue-700 shadow-sm'
                  : 'text-gray-600 hover:text-blue-600 hover:bg-gray-50'
              }`}
            >
              <User className="w-4 h-4 inline mr-2" />
              Book Flights
            </button>
            <button
              onClick={() => onViewChange('admin')}
              className={`px-4 py-2 rounded-lg font-medium transition-all duration-200 ${
                currentView === 'admin'
                  ? 'bg-blue-100 text-blue-700 shadow-sm'
                  : 'text-gray-600 hover:text-blue-600 hover:bg-gray-50'
              }`}
            >
              <Settings className="w-4 h-4 inline mr-2" />
              Admin
            </button>
          </nav>
        </div>
      </div>
    </header>
  );
};