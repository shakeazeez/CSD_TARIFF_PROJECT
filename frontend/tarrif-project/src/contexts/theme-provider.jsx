/**
 * Theme Provider Context - Dark/Light Mode Management
 *
 * Provides application-wide theme management with support for:
 * - Light mode: Explicit light theme
 * - Dark mode: Explicit dark theme
 * - System mode: Follows user's OS preference
 *
 * Features:
 * - Persistent theme storage in localStorage
 * - Automatic system preference detection
 * - Dynamic CSS class management on document root
 * - React Context for global theme state
 *
 * Usage:
 * 1. Wrap app with <ThemeProvider>
 * 2. Use useTheme() hook to access/change theme
 * 3. Theme automatically applies to Tailwind's dark: prefix classes
 */

import { createContext, useEffect, useState } from "react"
import { customColors } from "./theme-utils.js"

// Create context with default values and type safety
const ThemeProviderContext = createContext({
  theme: "system",
  setTheme: () => null,
})

/**
 * ThemeProvider Component
 *
 * Manages application theme state and DOM class application.
 * Handles theme persistence and system preference detection.
 */
export function ThemeProvider({
  children,
  defaultTheme = "system",
  storageKey = "vite-ui-theme",
  ...props
}) {
  // Initialize theme from localStorage or use default
  const [theme, setTheme] = useState(
    () => localStorage.getItem(storageKey) || defaultTheme
  )

  // Effect to apply theme changes to DOM
  useEffect(() => {
    const root = window.document.documentElement

    // Remove existing theme classes
    root.classList.remove("light", "dark")

    // Handle system theme detection
    if (theme === "system") {
      const systemTheme = window.matchMedia("(prefers-color-scheme: dark)")
        .matches
        ? "dark"
        : "light"

      root.classList.add(systemTheme)
      return
    }

    // Apply explicit theme choice
    root.classList.add(theme)
  }, [theme])

  // Context value with theme state and setter
  const currentTheme = theme === "system" 
    ? (window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light")
    : theme

  const value = {
    theme,
    setTheme: (theme) => {
      localStorage.setItem(storageKey, theme)  // Persist to localStorage
      setTheme(theme)                          // Update state
    },
    colors: customColors[currentTheme],
    toggleTheme: () => {
      const newTheme = currentTheme === "light" ? "dark" : "light"
      localStorage.setItem(storageKey, newTheme)
      setTheme(newTheme)
    },
    isDark: currentTheme === "dark",
    isLight: currentTheme === "light"
  }

  return (
    <ThemeProviderContext.Provider {...props} value={value}>
      {children}
    </ThemeProviderContext.Provider>
  )
}

// Export the context for use in the useTheme hook
export { ThemeProviderContext }