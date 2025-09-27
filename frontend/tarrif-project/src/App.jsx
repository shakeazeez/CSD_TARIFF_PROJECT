import { useState } from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate} from 'react-router-dom'
import './App.css'
import { Home } from './pages/Home.jsx'
import { Calculator } from './pages/Calculator.jsx'
import { FAQ } from './pages/FAQ.jsx'
import { Login } from './pages/Login.jsx'
import { Dashboard } from './pages/Dashboard.jsx'
import ComingSoon from './pages/ComingSoon.jsx'
import Sidebar from './components/Sidebar.jsx'
import WorldMapRoutes from './components/worldmaproutes.jsx'
import { ThemeProvider } from './contexts/ThemeContext.jsx'
import { AuthProvider, useAuth } from './contexts/AuthContext.jsx'
import { Footer } from './components/Footer.jsx'
import { NotFound } from './pages/NotFound.jsx'
import { Toaster } from './components/Toaster.jsx'
import './utils/themeUtils.js' // Import theme debugging utilities

/**
 * Protected Route component that redirects to login if not authenticated
 */
const ProtectedRoute = ({ children }) => {
    const { isAuthenticated, loading } = useAuth();

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-gray-900"></div>
            </div>
        );
    }

    return isAuthenticated ? children : <Navigate to="/" replace />;
};

/**
 * Public Route component that redirects to home if already authenticated
 */
const PublicRoute = ({ children }) => {
    const { isAuthenticated, loading } = useAuth();

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-gray-900"></div>
            </div>
        );
    }

    return !isAuthenticated ? children : <Navigate to="/" replace />;
};

/**
 * Main App component with authentication and theme provider integration
 *
 * Features:
 * - Custom color scheme with component-level theme detection
 * - Authentication state management
 * - Protected and public routes
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
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <ThemeProvider>
      <AuthProvider>
        {/* World Map Background */}
        <WorldMapRoutes background={true} />

        <Router>
          {/* Sidebar */}
          <Sidebar isOpen={sidebarOpen} setIsOpen={setSidebarOpen} />

          {/* Main Content */}
          <div className="relative z-10">
            <Routes>
              <Route path="/login" element={<Login />} />
              <Route path="/dashboard" element={
                <ProtectedRoute>
                  <Dashboard onMenuClick={() => setSidebarOpen(true)} />
                </ProtectedRoute>
              }/>
              <Route path="/calculator" element={
                <Calculator onMenuClick={() => setSidebarOpen(true)} />
              }/>
              <Route path="/faq" element={
                <FAQ onMenuClick={() => setSidebarOpen(true)} />
              }/>
              <Route path="/settings" element={
                <ComingSoon feature="Settings" />
              }/>
              <Route path="/" element={
                <Home onMenuClick={() => setSidebarOpen(true)} />
              }/>

              <Route path="*" element={<NotFound/>}/>
            </Routes>
          </div>
        </Router>
        <Footer />
        <Toaster />
      </AuthProvider>
    </ThemeProvider>
  )
}

export default App
