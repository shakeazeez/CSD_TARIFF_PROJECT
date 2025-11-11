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
import { ThemeProvider } from './contexts/theme-provider.jsx'
import { AuthProvider } from './contexts/AuthContext.jsx'
import { useAuth } from './contexts/use-auth.js'
import { Footer } from './components/Footer.jsx'
import { NotFound } from './pages/NotFound.jsx'
import { Toaster } from './components/Toaster.jsx'
import './utils/themeUtils.js' // Import theme debugging utilities
import { Business } from './pages/Business.jsx'
import { Bank } from './pages/Bank.jsx'
import { ChatBot } from './pages/ChatBot.jsx'
import { News } from './pages/News.jsx'

/**
 * Protected Route component that redirects to login if not authenticated
 * Enforces role-based access (allowedRoles: array of role strings)
 */
const ProtectedRoute = ({ children, allowedRoles = [] }) => {
    const { isAuthenticated, loading, user } = useAuth();

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-gray-900"></div>
            </div>
        );
    }

    if (!isAuthenticated) {
        return <Navigate to="/" replace />;
    }

    // if allowedRoles provided, check user's role
    if (allowedRoles.length > 0) {
        const userRole = user?.role || '';
        const hasAccess = allowedRoles.some( // checks if the user's role matches any allowed role for certain route
            role => userRole.toUpperCase() === role.toUpperCase()
        );
        
        if (!hasAccess) {
            return <Navigate to="/" replace />;
        }
    }

    return children;
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
        {/* World Map Background - Now responsive */}
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
              <Route path="/business" element={
                <ProtectedRoute allowedRoles={["BUSINESS"]}>
                  <Business onMenuClick={() => setSidebarOpen(true)} />
                </ProtectedRoute>
              }/>
              <Route path="/faq" element={
                <FAQ onMenuClick={() => setSidebarOpen(true)} />
              }/>
              <Route path="/chatbot" element={
                <ChatBot onMenuClick={() => setSidebarOpen(true)} />
              }/>
              <Route path="/news" element={
                <News onMenuClick={() => setSidebarOpen(true)} />
              }/>
              <Route path="/settings" element={
                <ComingSoon feature="Settings" />
              }/>
              <Route path="/trends" element={
                <ComingSoon feature="Tariff Trends" />
              }/>
              <Route path="/globalcoverage" element={
                <ComingSoon feature="Global Coverage" />
              }/>
              <Route path="/" element={
                <Home onMenuClick={() => setSidebarOpen(true)} />
              }/>
              <Route path="/bank" element={
                <ProtectedRoute allowedRoles={["BANK"]}>
                  <Bank onMenuClick={() => setSidebarOpen(true)} />
                </ProtectedRoute>
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
