import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useTheme } from '../contexts/ThemeContext';
import TariffChart from '../components/TariffChart';

const Dashboard = () => {
  const [history, setHistory] = useState([]);
  const [news, setNews] = useState([]);
  const { user } = useAuth();
  const { isDarkMode } = useTheme();

  useEffect(() => {
    // TODO: Fetch user history from API
    const fetchHistory = async () => {
      try {
        const response = await fetch('/api/user/history');
        if (response.ok) {
          const data = await response.json();
          setHistory(data);
        }
      } catch (err) {
        console.error('Failed to fetch history');
      }
    };

    // TODO: Fetch news from API
    const fetchNews = async () => {
      try {
        const response = await fetch('/api/news');
        if (response.ok) {
          const data = await response.json();
          setNews(data);
        }
      } catch (err) {
        console.error('Failed to fetch news');
      }
    };

    fetchHistory();
    fetchNews();
  }, []);

  return (
    <div className={`min-h-screen p-8 ${isDarkMode ? 'bg-gray-900 text-white' : 'bg-[#fefefe] text-gray-900'}`}>
      <div className="max-w-6xl mx-auto">
        <h1 className="text-4xl font-bold mb-8">Dashboard</h1>
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <div className={`p-6 rounded-lg shadow-lg ${isDarkMode ? 'bg-gray-800' : 'bg-[#efebd8]'}`}>
            <h2 className="text-2xl font-bold mb-4">Tariff History</h2>
            {history.length > 0 ? (
              <ul>
                {history.map((item, index) => (
                  <li key={index} className="mb-2">
                    {item.origin} to {item.destination}: ${item.tariff}
                  </li>
                ))}
              </ul>
            ) : (
              <p>No history available.</p>
            )}
          </div>
          <div className={`p-6 rounded-lg shadow-lg ${isDarkMode ? 'bg-gray-800' : 'bg-[#efebd8]'}`}>
            <h2 className="text-2xl font-bold mb-4">News</h2>
            {news.length > 0 ? (
              <ul>
                {news.map((item, index) => (
                  <li key={index} className="mb-2">
                    <a href={item.link} className="text-blue-500 hover:underline">{item.title}</a>
                  </li>
                ))}
              </ul>
            ) : (
              <p>No news available.</p>
            )}
          </div>
        </div>
        <div className={`mt-8 p-6 rounded-lg shadow-lg ${isDarkMode ? 'bg-gray-800' : 'bg-[#efebd8]'}`}>
          <h2 className="text-2xl font-bold mb-4">Tariff Visualization</h2>
          <TariffChart data={history} />
        </div>
      </div>
    </div>
  );
};

export default Dashboard;