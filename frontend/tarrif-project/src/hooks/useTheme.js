import { useContext } from 'react';
import ThemeContext from '../contexts/ThemeContext.jsx';

/**
 * Custom hook to use the theme context
 *
 * @returns {Object} Theme context value containing theme, colors, toggleTheme, isDark, isLight
 */
export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};