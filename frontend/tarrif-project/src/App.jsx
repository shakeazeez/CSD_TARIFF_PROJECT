import { useState } from 'react'
import { BrowserRouter as Router, Routes, Route} from 'react-router-dom'
import './App.css'
import { Home } from './pages/Home.jsx'
import { ThemeProvider } from './contexts/ThemeContext.jsx'
import './utils/themeUtils.js' // Import theme debugging utilities

/**
 * Main App component with custom theme provider integration
 * 
 * Features:
 * - Custom color scheme with component-level theme detection
 * - React Router for navigation
 * - Theme persistence and system preference detection
 * - Debug utilities for theme validation
 * 
 * Color Scheme:
 * - Green Accent: #ABCEBF
 * - Background: #F7F6EE
 * - Grey Bubbles: #F5F5F5
 */
function App() {

  return (
    <ThemeProvider>
      <Router>
        <Routes>
          <Route path="/" element={<Home/>}/>
        </Routes>
      </Router>
    </ThemeProvider>
  )
}

export default App
