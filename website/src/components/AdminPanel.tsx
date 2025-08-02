import React, { useState } from 'react';
import { Upload, FileSpreadsheet, Download, AlertCircle, CheckCircle } from 'lucide-react';
import { uploadFlightSheet } from '../services/api';

export const AdminPanel: React.FC = () => {
  const [uploadFile, setUploadFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [uploadResult, setUploadResult] = useState<{ success: boolean; processed: number } | null>(null);

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setUploadFile(file);
      setUploadResult(null);
    }
  };

  const handleUpload = async () => {
    if (!uploadFile) return;

    setUploading(true);
    try {
      const result = await uploadFlightSheet(uploadFile);
      setUploadResult(result);
    } catch (error) {
      console.error('Upload failed:', error);
      setUploadResult({ success: false, processed: 0 });
    } finally {
      setUploading(false);
    }
  };

  const downloadTemplate = () => {
    const csvContent = `data:text/csv;charset=utf-8,Airline,Flight Number,Departure Airport,Arrival Airport,Departure Date,Departure Time,Arrival Date,Arrival Time,Duration,Price,Currency,Available Seats,Aircraft,Stops
Credair Express,CE101,NYC,LAX,2024-02-15,08:00,2024-02-15,11:30,5h 30m,299,USD,42,Boeing 737-800,0
Credair Premium,CP205,NYC,LAX,2024-02-15,14:15,2024-02-15,17:45,5h 30m,449,USD,28,Airbus A320,0`;

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', 'flight_template.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="max-w-4xl mx-auto p-6">
      <div className="bg-white rounded-xl shadow-lg p-8">
        <h2 className="text-2xl font-bold text-gray-900 mb-6">Flight Management</h2>
        
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Upload Section */}
          <div className="space-y-6">
            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Upload Flight Data</h3>
              <p className="text-gray-600 mb-4">
                Upload a CSV file with flight information to add multiple flights at once.
              </p>
              
              <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-blue-400 transition-colors duration-200">
                <input
                  id="file-upload"
                  type="file"
                  accept=".csv"
                  onChange={handleFileSelect}
                  className="hidden"
                />
                <label
                  htmlFor="file-upload"
                  className="cursor-pointer flex flex-col items-center space-y-2"
                >
                  <FileSpreadsheet className="w-12 h-12 text-gray-400" />
                  <p className="text-gray-600">
                    {uploadFile ? uploadFile.name : 'Click to select CSV file'}
                  </p>
                  <p className="text-sm text-gray-500">CSV files only</p>
                </label>
              </div>
              
              {uploadFile && (
                <button
                  onClick={handleUpload}
                  disabled={uploading}
                  className="w-full bg-blue-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center space-x-2"
                >
                  <Upload className="w-5 h-5" />
                  <span>{uploading ? 'Uploading...' : 'Upload Flight Data'}</span>
                </button>
              )}
            </div>

            {/* Upload Result */}
            {uploadResult && (
              <div className={`p-4 rounded-lg flex items-center space-x-3 ${
                uploadResult.success 
                  ? 'bg-green-50 border border-green-200' 
                  : 'bg-red-50 border border-red-200'
              }`}>
                {uploadResult.success ? (
                  <CheckCircle className="w-5 h-5 text-green-600" />
                ) : (
                  <AlertCircle className="w-5 h-5 text-red-600" />
                )}
                <div>
                  <p className={`font-medium ${
                    uploadResult.success ? 'text-green-800' : 'text-red-800'
                  }`}>
                    {uploadResult.success ? 'Upload Successful' : 'Upload Failed'}
                  </p>
                  {uploadResult.success && (
                    <p className="text-sm text-green-600">
                      {uploadResult.processed} flights processed and added to the system.
                    </p>
                  )}
                </div>
              </div>
            )}
          </div>

          {/* Template Section */}
          <div className="space-y-6">
            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-4">CSV Template</h3>
              <p className="text-gray-600 mb-4">
                Download the CSV template to ensure your flight data is in the correct format.
              </p>
              
              <button
                onClick={downloadTemplate}
                className="w-full bg-gray-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-all duration-200 flex items-center justify-center space-x-2"
              >
                <Download className="w-5 h-5" />
                <span>Download CSV Template</span>
              </button>
            </div>

            {/* Instructions */}
            <div className="bg-blue-50 rounded-lg p-6">
              <h4 className="font-semibold text-blue-900 mb-3">File Format Requirements</h4>
              <ul className="text-sm text-blue-800 space-y-1">
                <li>• CSV format with comma separators</li>
                <li>• First row must contain headers</li>
                <li>• All required fields must be filled</li>
                <li>• Date format: YYYY-MM-DD</li>
                <li>• Time format: HH:MM (24-hour)</li>
                <li>• Price should be numeric (no currency symbols)</li>
              </ul>
            </div>
          </div>
        </div>

        {/* Statistics */}
        <div className="mt-8 grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-gradient-to-r from-blue-600 to-teal-600 rounded-lg p-6 text-white">
            <h4 className="text-lg font-semibold mb-2">Total Flights</h4>
            <p className="text-3xl font-bold">247</p>
          </div>
          <div className="bg-gradient-to-r from-green-600 to-emerald-600 rounded-lg p-6 text-white">
            <h4 className="text-lg font-semibold mb-2">Active Routes</h4>
            <p className="text-3xl font-bold">58</p>
          </div>
          <div className="bg-gradient-to-r from-purple-600 to-pink-600 rounded-lg p-6 text-white">
            <h4 className="text-lg font-semibold mb-2">Monthly Bookings</h4>
            <p className="text-3xl font-bold">1,234</p>
          </div>
        </div>
      </div>
    </div>
  );
};