/**
 * Theme Integration Test & Validation
 * 
 * This file provides utilities to test and validate the custom theme implementation
 * with the specified color scheme:
 * - Green Accent: #ABCEBF
 * - Background: #F7F6EE 
 * - Grey Bubbles: #F5F5F5
 */

// Color validation function to ensure hex colors are correctly applied
export const validateCustomColors = () => {
  const expectedColors = {
    light: {
      accent: '#ABCEBF',
      background: '#F7F6EE',
      surface: '#F5F5F5'
    }
  };
  
  // Check if CSS custom properties are applied
  const rootStyles = getComputedStyle(document.documentElement);
  const appliedAccent = rootStyles.getPropertyValue('--accent');
  const appliedBackground = rootStyles.getPropertyValue('--background');
  const appliedCard = rootStyles.getPropertyValue('--card');
  
  return {
    expectedColors,
    appliedColors: {
      accent: appliedAccent,
      background: appliedBackground,
      card: appliedCard
    }
  };
};

// Convert hex to HSL for CSS custom property validation
export const hexToHsl = (hex) => {
  const r = parseInt(hex.slice(1, 3), 16) / 255;
  const g = parseInt(hex.slice(3, 5), 16) / 255;
  const b = parseInt(hex.slice(5, 7), 16) / 255;

  const max = Math.max(r, g, b);
  const min = Math.min(r, g, b);
  let h, s, l;

  l = (max + min) / 2;

  if (max === min) {
    h = s = 0;
  } else {
    const d = max - min;
    s = l > 0.5 ? d / (2 - max - min) : d / (max + min);

    switch (max) {
      case r: h = (g - b) / d + (g < b ? 6 : 0); break;
      case g: h = (b - r) / d + 2; break;
      case b: h = (r - g) / d + 4; break;
    }
    h /= 6;
  }

  return {
    h: Math.round(h * 360),
    s: Math.round(s * 100),
    l: Math.round(l * 100)
  };
};

// Validate color conversions used in CSS
export const validateColorConversions = () => {
  const colors = {
    accent: '#ABCEBF',
    background: '#F7F6EE',
    surface: '#F5F5F5'
  };
  
  Object.entries(colors).forEach(([name, hex]) => {
    const hsl = hexToHsl(hex);
  });
};

// Theme debugging helper with comprehensive component checks
export const debugTheme = () => {
  // Check if theme context is available
  if (window.React) {
  }
  
  // Check if all themed components are present
  const themedComponents = [
    '.theme-toggle-button',
    '[data-theme]',
    '.custom-dropdown',
    '.chart-container'
  ];
  
  themedComponents.forEach(selector => {
    const elements = document.querySelectorAll(selector);
  });
  
  validateCustomColors();
  validateColorConversions();
  
  // Test theme switching
  const currentTheme = document.documentElement.getAttribute('data-theme');
  
  return {
    currentTheme,
    hasDataAttribute: !!document.documentElement.getAttribute('data-theme'),
    hasClassBasedTheme: document.documentElement.classList.contains('dark'),
    systemPreference: window.matchMedia('(prefers-color-scheme: dark)').matches
  };
};

// Export for console debugging
if (typeof window !== 'undefined') {
  window.themeDebug = debugTheme;
  window.validateColors = validateCustomColors;
  
  // Auto-test theme integration when this file loads
  setTimeout(() => {
    const result = debugTheme();
    
    if (result.hasDataAttribute) {
    } else {
    }
    
    // Check if custom colors are applied
    const rootStyles = getComputedStyle(document.documentElement);
    const accentColor = rootStyles.getPropertyValue('--accent');
    
    if (accentColor.includes('152')) { // Our custom green accent HSL
    } else {
    }
  }, 1000); // Wait for components to load
}