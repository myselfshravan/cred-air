# Cred-Air Website

This is the frontend for the Cred-Air airline aggregation system, built with React, Vite, and Tailwind CSS.

## Getting Started

To get the frontend running locally, follow these steps:

1.  **Install dependencies:**

    ```bash
    yarn install
    ```

2.  **Run the development server:**

    ```bash
    yarn dev
    ```

    This will start the development server, typically at `http://localhost:5173`.

## Available Scripts

-   `yarn dev`: Starts the development server.
-   `yarn build`: Builds the application for production.
-   `yarn lint`: Lints the codebase for potential errors.
-   `yarn preview`: Serves the production build locally for preview.

## Technology Stack

-   **Framework**: [React](https://reactjs.org/)
-   **Build Tool**: [Vite](https://vitejs.dev/)
-   **Language**: [TypeScript](https://www.typescriptlang.org/)
-   **Styling**: [Tailwind CSS](https://tailwindcss.com/)
-   **Routing**: [React Router](https://reactrouter.com/)

## Project Structure

```
website/
├── src/
│   ├── components/          # Reusable UI components
│   │   ├── AdminPanel.tsx   # Admin functionality
│   │   ├── BookingConfirmation.tsx
│   │   ├── FlightCard.tsx   # Flight display component
│   │   ├── FlightDetailPage.tsx
│   │   ├── FlightTimelineView.tsx
│   │   ├── Header.tsx       # Navigation header
│   │   ├── PriceBreakdown.tsx
│   │   ├── SearchForm.tsx   # Flight search form
│   │   └── __tests__/       # Component unit tests
│   ├── screens/             # Page-level components
│   │   ├── ConfirmationScreen.tsx
│   │   ├── FlightDetailsScreen.tsx
│   │   ├── ResultsScreen.tsx
│   │   ├── SearchScreen.tsx
│   │   └── index.ts
│   ├── services/            # API and external service calls
│   │   ├── api.ts          # Backend API integration
│   │   └── __tests__/      # Service tests
│   ├── types/              # TypeScript type definitions
│   │   └── flight.ts       # Flight-related types
│   ├── App.tsx             # Main application component
│   ├── main.tsx            # Application entry point
│   └── index.css           # Global styles
├── public/                 # Static assets
├── dist/                   # Build output
├── package.json            # Dependencies and scripts
├── vite.config.ts          # Vite configuration
├── tailwind.config.js      # Tailwind CSS configuration
├── tsconfig.json           # TypeScript configuration
└── README.md              # This file
```

## Key Components

### SearchForm
- Flight search input form
- Date picker integration
- Passenger selection
- Validation and error handling

### FlightCard
- Individual flight result display
- Pricing information
- Flight details (times, duration, airline)
- Booking action buttons

### FlightDetailPage
- Detailed flight information
- Seat selection interface
- Passenger information forms
- Payment integration

### BookingConfirmation
- Booking success display
- Booking reference details
- Email confirmation
- Travel itinerary

## API Integration

### Service Layer (`src/services/api.ts`)
- RESTful API client for backend services
- Flight search endpoints
- Booking management
- Error handling and retry logic

### Backend Services Integration
- **Flight Search Service** (Port 8081): Flight search functionality
- **Flight Booking Service** (Port 8082): Booking management
- **Airline Management Service** (Port 8083): Airline data

## Styling and Design

### Tailwind CSS
- Utility-first CSS framework
- Responsive design patterns
- Custom color palette
- Component-based styling

### Design System
- Consistent spacing and typography
- Color scheme for different states
- Interactive elements and animations
- Accessibility considerations

## Development Workflow

### Local Development
1. Ensure backend services are running
2. Install dependencies with `yarn install`
3. Start development server with `yarn dev`
4. Access application at `http://localhost:5173`

### Code Quality
- ESLint for code linting
- TypeScript for type safety
- Jest for unit testing
- Prettier for code formatting

### Testing Strategy
- Component unit tests with React Testing Library
- Service layer tests for API integration
- End-to-end testing capabilities
- Mock data for development and testing

## Environment Configuration

### Development Environment
```bash
# Backend service URLs
VITE_FLIGHT_SEARCH_URL=http://localhost:8081
VITE_FLIGHT_BOOKING_URL=http://localhost:8082
VITE_AIRLINE_MGT_URL=http://localhost:8083
```

### Production Environment
- Environment-specific configuration
- CDN integration for static assets
- Performance optimization
- Security headers and HTTPS

## Features

### Flight Search
- Multi-city search capabilities
- Date range selection
- Passenger count and class selection
- Real-time search results

### Booking Flow
- Flight selection and review
- Passenger information collection
- Payment processing integration
- Booking confirmation and email

### User Experience
- Responsive design for all devices
- Loading states and error handling
- Progressive web app capabilities
- Offline functionality (planned)

## Performance Optimizations

### Build Optimizations
- Code splitting for route-based chunks
- Tree shaking for unused code elimination
- Asset optimization and compression
- Bundle analysis and size monitoring

### Runtime Optimizations
- React component memoization
- Lazy loading for non-critical components
- Image optimization and lazy loading
- Caching strategies for API responses

## Deployment

### Build Process
```bash
yarn build
```

### Production Deployment
- Static file hosting (Netlify, Vercel, S3)
- CDN integration for global distribution
- CI/CD pipeline integration
- Environment-specific builds

## Future Enhancements

### Planned Features
- User authentication and profiles
- Booking history and management
- Real-time flight status updates
- Multi-language support
- Push notifications

### Technical Improvements
- Progressive Web App (PWA) features
- Advanced caching strategies
- Performance monitoring
- A/B testing framework
- Analytics integration

## Troubleshooting

### Common Issues
1. **Port conflicts**: Ensure port 5173 is available
2. **Backend connectivity**: Verify backend services are running
3. **Dependency issues**: Clear node_modules and reinstall
4. **Build failures**: Check TypeScript errors and ESLint warnings

### Development Tips
- Use React Developer Tools for debugging
- Monitor network requests in browser DevTools
- Check console for error messages
- Use TypeScript language server for IDE support