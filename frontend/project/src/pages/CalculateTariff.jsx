import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useTheme } from '../contexts/ThemeContext';

const CalculateTariff = () => {
  const [form, setForm] = useState({
    originCountry: '',
    destinationCountry: '',
    itemCode: '',
    quantity: '',
    value: '',
    currency: 'USD',
    date: '',
  });
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const { isAuthenticated } = useAuth();
  const { isDarkMode } = useTheme();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleCalculate = async () => {
    try {
      // TODO: Replace with actual API call to calculate tariff
      const response = await fetch('/api/tariffs/calculate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      });
      if (response.ok) {
        const data = await response.json();
        setResult(data);
      } else {
        setError('Calculation failed');
      }
    } catch (err) {
      setError('Calculation failed');
    }
  };

  const handleHistory = () => {
    if (!isAuthenticated) {
      navigate('/login');
    } else {
      navigate('/dashboard');
    }
  };

  return (
    <div className={`min-h-screen p-8 ${isDarkMode ? 'bg-gray-900 text-white' : 'bg-[#fefefe] text-gray-900'}`}>
      <div className="max-w-4xl mx-auto">
        <h1 className="text-4xl font-bold mb-8 text-center">Tariff Calculator</h1>
        <div className={`p-6 rounded-lg shadow-lg mb-8 ${isDarkMode ? 'bg-gray-800' : 'bg-[#efebd8]'}`}>
          <form className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium mb-2">Origin Country</label>
              <input
                type="text"
                name="originCountry"
                value={form.originCountry}
                onChange={handleChange}
                className={`w-full p-3 rounded border ${isDarkMode ? 'bg-gray-700 border-gray-600' : 'bg-white border-gray-300'}`}
                placeholder="e.g., US"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">Destination Country</label>
              <input
                type="text"
                name="destinationCountry"
                value={form.destinationCountry}
                onChange={handleChange}
                className={`w-full p-3 rounded border ${isDarkMode ? 'bg-gray-700 border-gray-600' : 'bg-white border-gray-300'}`}
                placeholder="e.g., IN"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">Item Code (HS Code)</label>
              <input
                type="text"
                name="itemCode"
                value={form.itemCode}
                onChange={handleChange}
                className={`w-full p-3 rounded border ${isDarkMode ? 'bg-gray-700 border-gray-600' : 'bg-white border-gray-300'}`}
                placeholder="e.g., 123456"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">Quantity</label>
              <input
                type="number"
                name="quantity"
                value={form.quantity}
                onChange={handleChange}
                className={`w-full p-3 rounded border ${isDarkMode ? 'bg-gray-700 border-gray-600' : 'bg-white border-gray-300'}`}
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">Value</label>
              <input
                type="number"
                name="value"
                value={form.value}
                onChange={handleChange}
                className={`w-full p-3 rounded border ${isDarkMode ? 'bg-gray-700 border-gray-600' : 'bg-white border-gray-300'}`}
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">Currency</label>
              <select
                name="currency"
                value={form.currency}
                onChange={handleChange}
                className={`w-full p-3 rounded border ${isDarkMode ? 'bg-gray-700 border-gray-600' : 'bg-white border-gray-300'}`}
              >
                <option value="USD">USD</option>
                <option value="EUR">EUR</option>
                <option value="INR">INR</option>
              </select>
            </div>
            <div className="md:col-span-2">
              <label className="block text-sm font-medium mb-2">Transaction Date</label>
              <input
                type="date"
                name="date"
                value={form.date}
                onChange={handleChange}
                className={`w-full p-3 rounded border ${isDarkMode ? 'bg-gray-700 border-gray-600' : 'bg-white border-gray-300'}`}
              />
            </div>
          </form>
          <button
            onClick={handleCalculate}
            className={`mt-6 w-full p-3 rounded transition-transform hover:scale-105 ${isDarkMode ? 'bg-[#bed9cc] text-gray-900' : 'bg-[#6d6961] text-white'}`}
          >
            Calculate Tariff
          </button>
        </div>
        {result && (
          <div className={`p-6 rounded-lg shadow-lg ${isDarkMode ? 'bg-gray-800' : 'bg-[#dbd9c5]'}`}>
            <h2 className="text-2xl font-bold mb-4">Calculation Result</h2>
            <p>Total Charges: ${result.totalCharges}</p>
            <p>Breakdown: {result.breakdown}</p>
          </div>
        )}
        {error && <p className="text-red-500">{error}</p>}
        <button
          onClick={handleHistory}
          className={`mt-8 block mx-auto px-6 py-3 rounded transition-transform hover:scale-105 ${isDarkMode ? 'bg-[#bed9cc] text-gray-900' : 'bg-[#6d6961] text-white'}`}
        >
          View History
        </button>
      </div>
    </div>
  );
};

export default CalculateTariff;