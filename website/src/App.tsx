import {useState} from 'react';
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import {Header} from './components/Header';
import {ConfirmationScreen, FlightDetailsScreen, PaymentScreen, ResultsScreen, SearchScreen} from './screens';

type AppView = 'search' | 'admin';

function AppContent() {
  const [currentView, setCurrentView] = useState<AppView>('search');

  return (
    <div className="min-h-screen bg-gray-50">
      <Header currentView={currentView} onViewChange={setCurrentView} />
      
      <Routes>
        <Route path="/" element={<SearchScreen currentView={currentView} onViewChange={setCurrentView} />} />
        <Route path="/results" element={<ResultsScreen />} />
        <Route path="/flight-details" element={<FlightDetailsScreen />} />
        <Route path="/payment" element={<PaymentScreen />} />
        <Route path="/confirmation" element={<ConfirmationScreen />} />
      </Routes>
    </div>
  );
}

function App() {
  return (
    <Router>
      <AppContent />
    </Router>
  );
}

export default App;