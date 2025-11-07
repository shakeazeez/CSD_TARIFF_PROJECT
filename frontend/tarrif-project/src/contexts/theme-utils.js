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

/**
 * Utility function to get theme-aware colors outside of React components
 *
 * @param {string} themeName - 'light' or 'dark'
 * @returns {Object} Color palette for the specified theme
 */
export const getThemeColors = (themeName = 'light') => {
  return customColors[themeName];
};

export { customColors };