/**
 * ThemeToggle Component - Interactive Theme Switching Button
 * 
 * A user-friendly button that cycles through three theme modes:
 * 1. Light mode (☀️) - Explicit light theme
 * 2. Dark mode (🌙) - Explicit dark theme
 * 3. System mode (🖥️) - Follows OS preference
 * 
 * Features:
 * - Visual icons for each theme state (Sun, Moon, Monitor)
 * - Text labels (hidden on small screens for space)
 * - Smooth cycling through all three modes
 * - Accessible tooltips showing next mode
 * - Responsive design with proper contrast
 * 
 * The component integrates with the ThemeProvider context
 * to update global application theme state.
 */

import * as React from "react"
import { Moon, Sun, Monitor } from "lucide-react"
import { Button } from "./ui/button"
import { useTheme } from "../contexts/theme-provider"

export function ThemeToggle() {
  // Access theme state from context
  const { theme, setTheme } = useTheme()

  /**
   * Cycles through theme modes in sequence:
   * light → dark → system → light → ...
   */
  const toggleTheme = () => {
    if (theme === "light") {
      setTheme("dark")
    } else if (theme === "dark") {
      setTheme("system")
    } else {
      setTheme("light")
    }
  }

  /**
   * Returns appropriate icon component for current theme
   * @returns {JSX.Element} - Icon representing current theme mode
   */
  const getThemeIcon = () => {
    if (theme === "light") {
      return <Sun className="h-4 w-4" />
    } else if (theme === "dark") {
      return <Moon className="h-4 w-4" />
    } else {
      return <Monitor className="h-4 w-4" />
    }
  }

  /**
   * Returns human-readable label for current theme
   * @returns {string} - Theme mode label
   */
  const getThemeLabel = () => {
    if (theme === "light") return "Light"
    if (theme === "dark") return "Dark"
    return "System"
  }

  return (
    <Button
      variant="outline"
      size="sm"
      onClick={toggleTheme}
      className="flex items-center gap-2 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700"
      title={`Switch to ${theme === "light" ? "dark" : theme === "dark" ? "system" : "light"} mode`}
    >
      {getThemeIcon()}
      {/* Theme label - hidden on small screens for space efficiency */}
      <span className="hidden sm:inline text-gray-700 dark:text-gray-200">{getThemeLabel()}</span>
    </Button>
  )
}