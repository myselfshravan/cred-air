import React, {useState} from 'react';
import {Plane, Clock, ChevronUp, ChevronDown} from 'lucide-react';
import {FlightJourney} from '../types/flight';

interface FlightTimelineViewProps {
    flightJourney: FlightJourney;
}

export const FlightTimelineView: React.FC<FlightTimelineViewProps> = ({flightJourney}) => {
    const [isExpanded, setIsExpanded] = useState(true);
    const formatTime = (timestamp: number) => {
        return new Date(timestamp).toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        });
    };

    const formatDate = (timestamp: number) => {
        return new Date(timestamp).toLocaleDateString('en-US', {
            weekday: 'short',
            month: 'short',
            day: 'numeric'
        });
    };

    const formatDuration = (duration: number | undefined) => {
        if (!duration) return '0h 00m';

        // Convert to string if it's a number
        const durationStr = String(duration);

        const seconds = parseInt(durationStr);
        if (isNaN(seconds)) return durationStr;
        const minutes = Math.floor(seconds / 60);
        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;
        if (hours === 0) {
            return `${mins}m`;
        }
        if (mins === 0) {
            return `${hours}h`;
        }
        return `${hours}h ${mins.toString().padStart(2, '0')}m`;
    };

    const firstSegment = flightJourney.segments[0];
    const lastSegment = flightJourney.segments[flightJourney.segments.length - 1];
    const departureTime = formatTime(firstSegment.departure.departsAt);
    const arrivalTime = formatTime(lastSegment.arrival.arrivesAt);
    const arrivalDate = formatDate(lastSegment.arrival.arrivesAt);
    const departureDate = formatDate(firstSegment.departure.departsAt);
    const isNextDay = arrivalDate !== departureDate;

    return (
        <div className="bg-white rounded-xl shadow-lg border border-gray-200">
            {/* Compact Header Summary */}
            <div className="p-3 border-b border-gray-100">
                <div className="flex items-center justify-end">
                    <button
                        onClick={() => setIsExpanded(!isExpanded)}
                        className="p-1 hover:bg-gray-100 rounded-full transition-colors"
                    >
                        {isExpanded ? (
                            <ChevronUp className="w-4 h-4 text-blue-600"/>
                        ) : (
                            <ChevronDown className="w-4 h-4 text-blue-600"/>
                        )}
                    </button>
                </div>

                <div className="flex items-start justify-between mt-2">
                    <div className="text-left">
                        <div className="text-xl font-bold text-gray-900">{departureTime}</div>
                        <div className="text-xs text-gray-500 mt-1">{departureDate}</div>
                    </div>
                    <div className="flex-1 mx-4 flex flex-col items-center justify-center mt-2">
                        <div className="w-full relative flex items-center">
                            <div className="w-3 h-3 bg-blue-500 rounded-full border-2 border-white shadow-sm"></div>
                            <div className="flex-1 h-0.5 bg-gray-300 relative mx-2">
                                <div className="absolute inset-0 flex items-center justify-center">
                                    <div className="bg-white px-2 text-xs text-gray-600 font-medium">
                                        {formatDuration(flightJourney.totalDuration)}
                                    </div>
                                </div>
                            </div>
                            <div className="w-3 h-3 bg-green-500 rounded-full border-2 border-white shadow-sm"></div>
                        </div>
                        {flightJourney.totalStops > 0 && (
                            <div className="text-xs text-gray-500 mt-2">
                                {flightJourney.totalStops} stop{flightJourney.totalStops > 1 ? 's' : ''}
                                {flightJourney.layovers.length > 0 && ` in ${flightJourney.layovers.map(layover => layover.airportCode).join(', ')}`}
                            </div>
                        )}
                    </div>
                    <div className="text-right">
                        <div className="text-xl font-bold text-gray-900">
                            {arrivalTime}{isNextDay && <sup>+1</sup>}
                        </div>
                        <div className="text-xs text-gray-500 mt-1">{arrivalDate}</div>
                    </div>
                </div>

                <div className="flex items-center justify-between mt-1 text-sm">
                    <div>
                        <span className="font-medium">{firstSegment.departure.airportCode}</span>
                    </div>
                    <div className="text-right">
                        <span className="font-medium">{lastSegment.arrival.airportCode}</span>
                    </div>
                </div>
            </div>

            {/* Detailed Timeline */}
            {isExpanded && <div className="p-3 space-y-2">
                {flightJourney.segments.map((segment, segmentIndex) => {
                        const layover = flightJourney.layovers[segmentIndex];
                        const longWait = layover?.duration > 180;
                        return <div key={segmentIndex}>
                            {/* Flight Segment */}
                            <div className="flex items-start space-x-3">
                                <div className="flex flex-col items-center">
                                    <div className="w-6 h-6 flex items-center justify-center">
                                        {segment.airline.logoUrl && <img
                                            src={segment.airline.logoUrl}
                                            alt={`${segment.airline.name} logo`}
                                            className="w-5 h-5 object-contain"
                                        />}
                                    </div>
                                    {segmentIndex < flightJourney.segments.length - 1 &&
                                        <div className="w-px h-8 bg-gray-300 mt-1"></div>}
                                </div>

                                <div className="flex-1 min-w-0">
                                    <div className="flex items-center justify-between">
                                        <div className="text-sm text-gray-700">
                                        <span
                                            className="font-medium">{segment.airline.name}</span> {/* Flight number would go here */}
                                        </div>
                                    </div>

                                    <div className="flex items-center space-x-4 mt-1">
                                        <div className="flex items-center space-x-2">
                                            <div className="w-2 h-2 bg-gray-400 rounded-full"></div>
                                            <div>
                                                <div className="font-medium text-gray-900">
                                                    {formatTime(segment.departure.departsAt)}
                                                </div>
                                                <div
                                                    className="text-xs text-gray-500">{segment.departure.airportCode} {segment.departure.city}</div>
                                            </div>
                                        </div>

                                        <div className="flex-1 flex items-center justify-center">
                                            <div className="flex items-center space-x-1 text-xs text-gray-600">
                                                <Clock className="w-3 h-3"/>
                                                <span>{formatDuration(segment.segmentDuration)}</span>
                                            </div>
                                        </div>

                                        <div className="flex items-center space-x-2">
                                            <div className="text-right">
                                                <div className="font-medium text-gray-900">
                                                    {formatTime(segment.arrival.arrivesAt)}
                                                </div>
                                                <div
                                                    className="text-xs text-gray-500">{segment.arrival.airportCode} {segment.arrival.city}</div>
                                            </div>
                                            <div className="w-2 h-2 bg-gray-400 rounded-full"></div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {/* Layover */}
                            {segmentIndex < flightJourney.segments.length - 1 && flightJourney.layovers[segmentIndex] &&
                                <div className="ml-9 my-2">
                                    <div className="bg-gray-100 rounded-lg px-3 py-2">
                                        <div className="text-xs text-pink-600 font-medium">
                                            {formatDuration(flightJourney.layovers[segmentIndex]?.duration)} connect in
                                            airport {longWait ? `long wait` : null}
                                        </div>
                                    </div>
                                </div>}
                        </div>
                    }
                )}
            </div>}

            {/* Bottom Summary */}
            <div className="px-4 py-3 border-t border-gray-100 bg-gray-50 rounded-b-xl">
                <div className="text-xs text-gray-600">
                    <span className="font-medium">Arrives:</span> {arrivalDate} | <span className="font-medium">Journey duration:</span> {formatDuration(flightJourney.totalDuration)}
                </div>
            </div>
        </div>
    );
};