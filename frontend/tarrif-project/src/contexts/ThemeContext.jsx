import { createContext, useState, useEffect } from 'react';
import { customColors } from './theme-utils.js';// Theme context
const ThemeContext = createContext({
  theme: 'light',
  colors: customColors.light,
  toggleTheme: () => {},
  isDark: false,
  isLight: true
});

/**
 * Theme provider component that wraps the application
 * 
 * Features:
 * - Detects system theme preference
 * - Provides theme state and colors to all components
 * - Handles theme switching
 * - Persists theme preference in localStorage
 * 
 * @param {Object} props - Component props
 * @param {ReactNode} props.children - Child components
 */
export const ThemeProvider = ({ children }) => {
  // Initialize theme from localStorage or system preference
  const [theme, setTheme] = useState(() => {
    const saved = localStorage.getItem('theme');
    if (saved) return saved;
    
    // Fallback to system preference
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  });

  // Update theme when system preference changes
  useEffect(() => {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    
    const handleChange = (e) => {
      // Only update if no manual theme is set
      if (!localStorage.getItem('theme')) {
        setTheme(e.matches ? 'dark' : 'light');
      }
    };

    mediaQuery.addEventListener('change', handleChange);
    return () => mediaQuery.removeEventListener('change', handleChange);
  }, []);

  // Persist theme preference
  useEffect(() => {
    localStorage.setItem('theme', theme);
    
    // Update document class for global styling if needed
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  /**
   * Toggle between light and dark themes
   */
  const toggleTheme = () => {
    setTheme(prev => prev === 'light' ? 'dark' : 'light');
  };

  // Get current color palette
  const colors = customColors[theme];
  const isDark = theme === 'dark';
  const isLight = theme === 'light';

  const value = {
    theme,
    colors,
    toggleTheme,
    isDark,
    isLight
  };

  return (
    <ThemeContext.Provider value={value}>
      {children}
    </ThemeContext.Provider>
  );
};

export default ThemeContext;