import React from 'react';
import { useTheme } from '../contexts/ThemeContext';

const TariffChart = ({ data }) => {
  const { isDarkMode } = useTheme();

  // TODO: Implement chart using a library like Recharts
  // For now, placeholder
  return (
    <div className={`p-4 rounded ${isDarkMode ? 'bg-gray-700' : 'bg-[#dbd9c5]'}`}>
      <h3 className="text-lg font-bold mb-4">Tariff Trends</h3>
      <div className="h-64 flex items-center justify-center">
        <p>Chart Placeholder: Display tariff rates over time between countries for the item.</p>
        {/* Example: Use Recharts LineChart with data */}
        {/* Hover to show metadata like date, rate, item code */}
      </div>
      <ul className="mt-4">
        <li>Legend: Item Code, Country Pair, Effective Dates</li>
      </ul>
    </div>
  );
};

export default TariffChart;