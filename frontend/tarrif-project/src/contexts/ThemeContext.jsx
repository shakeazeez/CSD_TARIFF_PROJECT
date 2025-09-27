import { createContext, useContext, useState, useEffect } from 'react';

/**
 * Theme context for managing custom color scheme throughout the application
 * 
 * Custom Color Palette:
 * - Green Accent: #ABCEBF (used for highlights, buttons, active states)
 * - Background: #F7F6EE (main background color)
 * - Grey Bubbles: #F5F5F5 (for cards, containers, secondary elements)
 * 
 * Features:
 * - Component-level theme detection
 * - Automatic dark/light mode switching
 * - Centralized color management
 * - Easy theme toggling for future enhancements
 */

// Custom color definitions
const customColors = {
  light: {
    // Primary brand colors
    accent: '#ABCEBF',      // Green accent for buttons, highlights
    background: '#efeac2ff',   // Slightly darker warm off-white background
    surface: '#F0F0F0',     // Darker light grey for cards/bubbles
    
    // Text colors for good contrast
    foreground: '#1a1a1a',  // Dark text on light backgrounds
    muted: '#6b7280',       // Muted text for secondary content
    
    // Interactive states
    hover: '#9bc4af',       // Slightly darker accent for hover states
    border: '#e5e7eb',      // Subtle borders
    input: '#ffffff',       // Input backgrounds
    
    // Semantic colors
    success: '#10b981',
    warning: '#f59e0b',
    error: '#ef4444',
    info: '#3b82f6'
  },
  dark: {
    // Dark mode adaptations of custom colors
    accent: '#8bc4a3',      // Slightly muted green for dark mode
    background: '#1a1a1a',  // Dark background
    surface: '#2a2a2a',     // Dark grey for cards/bubbles
    
    // Text colors for dark mode
    foreground: '#f5f5f5',  // Light text on dark backgrounds
    muted: '#9ca3af',       // Muted text for secondary content
    
    // Interactive states
    hover: '#a3d4b7',       // Lighter accent for hover in dark mode
    border: '#374151',      // Dark borders
    input: '#374151',       // Input backgrounds
    
    // Semantic colors (adjusted for dark mode)
    success: '#059669',
    warning: '#d97706',
    error: '#dc2626',
    info: '#2563eb'
  }
};

// Theme context
const ThemeContext = createContext({
  theme: 'light',
  colors: customColors.light,
  toggleTheme: () => {},
  isDark: false,
  isLight: true
});

/**
 * Custom hook to access theme context
 * 
 * @returns {Object} Theme context with colors, theme state, and toggle function
 * 
 * Usage:
 * const { colors, theme, toggleTheme, isDark } = useTheme();
 */
export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};

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

/**
 * Higher-order component for wrapping components with theme context
 * 
 * @param {React.Component} Component - Component to wrap
 * @returns {React.Component} Wrapped component with theme props
 */
export const withTheme = (Component) => {
  return function ThemedComponent(props) {
    const theme = useTheme();
    return <Component {...props} theme={theme} />;
  };
};

/**
 * Utility function to get theme-aware colors outside of React components
 * 
 * @param {string} themeName - 'light' or 'dark'
 * @returns {Object} Color palette for the specified theme
 */
export const getThemeColors = (themeName = 'light') => {
  return customColors[themeName];
};

export default ThemeContext;