import { useContext } from 'react';
import { ThemeProviderContext } from './theme-provider.jsx';

/**
 * Custom hook to access theme context
 *
 * @returns {Object} Theme context with colors, theme state, and toggle function
 *
 * Usage:
 * const { colors, theme, toggleTheme, isDark } = useTheme();
 */
export const useTheme = () => {
  const context = useContext(ThemeProviderContext);
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};