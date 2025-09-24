import React from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useTheme } from '../contexts/ThemeContext';
import { useNavigate } from 'react-router-dom';

const Settings = () => {
  const { logout } = useAuth();
  const { isDarkMode, toggleTheme } = useTheme();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <div className={`min-h-screen p-8 ${isDarkMode ? 'bg-gray-900 text-white' : 'bg-[#fefefe] text-gray-900'}`}>
      <div className="max-w-4xl mx-auto">
        <h1 className="text-4xl font-bold mb-8">Settings</h1>
        <div className={`p-6 rounded-lg shadow-lg ${isDarkMode ? 'bg-gray-800' : 'bg-[#efebd8]'}`}>
          <h2 className="text-2xl font-bold mb-4">Preferences</h2>
          <div className="mb-4">
            <label className="block text-sm font-medium mb-2">Theme</label>
            <button
              onClick={toggleTheme}
              className={`px-4 py-2 rounded transition-transform hover:scale-105 ${isDarkMode ? 'bg-[#bed9cc] text-gray-900' : 'bg-[#6d6961] text-white'}`}
            >
              Switch to {isDarkMode ? 'Light' : 'Dark'} Mode
            </button>
          </div>
          <div className="mb-4">
            <label className="block text-sm font-medium mb-2">Notifications</label>
            <input type="checkbox" className="mr-2" /> Enable email notifications
          </div>
          <button
            onClick={handleLogout}
            className={`px-6 py-3 rounded transition-transform hover:scale-105 bg-red-500 text-white hover:bg-red-600`}
          >
            Logout
          </button>
        </div>
      </div>
    </div>
  );
};

export default Settings;