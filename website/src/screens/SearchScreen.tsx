import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { SearchForm } from '../components/SearchForm';
import { AdminPanel } from '../components/AdminPanel';
import { SearchParams } from '../types/flight';

type AppView = 'search' | 'admin';

interface SearchScreenProps {
  currentView: AppView;
  onViewChange: (view: AppView) => void;
}

export function SearchScreen({ currentView, onViewChange }: SearchScreenProps) {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSearch = async (params: SearchParams) => {
    setLoading(true);
    try {
      const searchParams = new URLSearchParams({
        from: params.from,
        to: params.to,
        departDate: params.departDate,
        returnDate: params.returnDate || '',
        passengers: params.passengers.toString(),
        class: params.class
      });
      navigate(`/results?${searchParams.toString()}`);
    } catch (error) {
      console.error('Search failed:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {currentView === 'admin' ? (
        <AdminPanel />
      ) : (
        <div>
          <div className="text-center mb-8">
            <h2 className="text-4xl font-bold text-gray-900 mb-4">
              Find Your Perfect Flight
            </h2>
            <p className="text-xl text-gray-600">
              Search and book flights with confidence on Credair
            </p>
          </div>
          <SearchForm onSearch={handleSearch} loading={loading} />
        </div>
      )}
    </main>
  );
}