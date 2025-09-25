import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Calculator,
  HelpCircle,
  Home,
  Menu,
  X,
  ArrowLeft,
  User,
  LogOut,
  Sun,
  Moon,
  BarChart3
} from 'lucide-react';
import { useTheme } from '../contexts/ThemeContext';
import { useAuth } from '../contexts/AuthContext';
import { Button } from './ui/button';

const Sidebar = ({ isOpen, setIsOpen }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { isDark, toggleTheme, colors } = useTheme();
  const { isAuthenticated, user, logout } = useAuth();

  const menuItems = [
    ...(isAuthenticated ? [] : [
      {
        icon: Home,
        label: 'Home',
        path: '/',
        description: 'Landing page with global trade overview'
      }
    ]),
    {
      icon: Calculator,
      label: 'Tariff Calculator',
      path: '/calculator',
      description: 'Calculate international tariffs'
    },
    {
      icon: HelpCircle,
      label: 'FAQ',
      path: '/faq',
      description: 'Frequently asked questions'
    },
    ...(isAuthenticated ? [
      {
        icon: BarChart3,
        label: 'My Dashboard',
        path: '/dashboard',
        description: 'View your tariff calculations and history'
      },
      {
        icon: User,
        label: 'Settings',
        path: '/settings',
        description: 'Account settings and preferences'
      }
    ] : [])
  ];

  const handleNavigation = (path, requiresAuth, comingSoon) => {
    if (comingSoon) {
      // Show coming soon message
      alert('This feature is coming soon!');
      return;
    }

    if (requiresAuth && !user) {
      navigate('/login');
      return;
    }

    navigate(path);
    setIsOpen(false);
  };

  const handleLogout = () => {
    logout();
    navigate('/');
    setIsOpen(false);
  };

  return (
    <>
      {/* Mobile overlay */}
      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 z-40 md:hidden"
            onClick={() => setIsOpen(false)}
          />
        )}
      </AnimatePresence>

      {/* Sidebar */}
      <motion.div
        initial={{ x: -320 }}
        animate={{ x: isOpen ? 0 : -320 }}
        transition={{ type: 'tween', duration: 0.3 }}
        className="fixed left-0 top-0 h-full w-80 bg-white/95 dark:bg-slate-900/95 backdrop-blur-lg border-r border-slate-200 dark:border-slate-700 z-50 shadow-xl"
        style={{
          backgroundColor: `${colors.surface}95`,
          borderColor: colors.border
        }}
      >
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-slate-200 dark:border-slate-700">
          <div className="flex items-center space-x-3">
            <div className="p-3 rounded-lg" style={{ backgroundColor: colors.accent }}>
              <img
                src="/tempGOAT.png"
                alt="GoatTariff Logo"
                className="h-12 w-12 object-contain"
              />
            </div>
          </div>

          <Button
            variant="ghost"
            size="sm"
            onClick={() => setIsOpen(false)}
            className="md:hidden"
            style={{
              backgroundColor: `${colors.surface}`,
              color: colors.foreground,
              border: `1px solid ${colors.border}`
            }}
          >
            <ArrowLeft className="h-5 w-5" />
          </Button>
        </div>

        {/* User Info */}
        {user && (
          <div className="p-4 border-b border-slate-200 dark:border-slate-700">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 rounded-full bg-gradient-to-r from-blue-500 to-purple-600 flex items-center justify-center">
                <span className="text-white text-sm font-semibold">
                  {user?.email?.charAt(0).toUpperCase() || 'U'}
                </span>
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium truncate" style={{ color: colors.foreground }}>
                  {user?.email || 'User'}
                </p>
                <p className="text-xs" style={{ color: colors.muted }}>
                  {user?.userType || user?.role || 'User'}
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Navigation Menu */}
        <nav className="flex-1 p-4">
          <div className="space-y-2">
            {menuItems.map((item) => {
              const isActive = location.pathname === item.path;
              const Icon = item.icon;

              return (
                <motion.button
                  key={item.path}
                  onClick={() => handleNavigation(item.path, item.requiresAuth, item.comingSoon)}
                  className={`w-full flex items-center space-x-3 px-4 py-3 rounded-lg transition-all duration-200 ${
                    isActive
                      ? 'bg-blue-500 text-white shadow-lg'
                      : 'hover:bg-slate-100 dark:hover:bg-slate-800'
                  }`}
                  style={{
                    backgroundColor: isActive ? colors.accent : 'transparent',
                    color: isActive ? '#ffffff' : colors.foreground
                  }}
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                >
                  <Icon className="h-5 w-5 flex-shrink-0" />
                  <div className="flex-1 text-left">
                    <div className="font-medium">{item.label}</div>
                    <div className={`text-xs ${isActive ? 'text-blue-100' : 'text-slate-500 dark:text-slate-400'}`}>
                      {item.description}
                    </div>
                  </div>
                  {item.comingSoon && (
                    <span className="text-xs bg-orange-500 text-white px-2 py-1 rounded-full">
                      Soon
                    </span>
                  )}
                </motion.button>
              );
            })}
          </div>
        </nav>

        {/* Footer */}
        <div className="p-4 border-t border-slate-200 dark:border-slate-700 space-y-3">
          {/* Theme Toggle */}
          <Button
            variant="outline"
            onClick={toggleTheme}
            className="w-full justify-start"
            style={{
              borderColor: colors.border,
              backgroundColor: colors.surface,
              color: colors.foreground
            }}
          >
            {isDark ? <Sun className="h-4 w-4 mr-3" /> : <Moon className="h-4 w-4 mr-3" />}
            {isDark ? 'Light Mode' : 'Dark Mode'}
          </Button>

          {/* Auth Buttons */}
          {isAuthenticated ? (
            <Button
              variant="outline"
              onClick={handleLogout}
              className="w-full justify-start text-red-600 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-950"
              style={{
                borderColor: colors.border,
                backgroundColor: colors.surface
              }}
            >
              <LogOut className="h-4 w-4 mr-3" />
              Logout
            </Button>
          ) : (
            <Button
              onClick={() => {
                navigate('/login');
                setIsOpen(false);
              }}
              className="w-full justify-start"
              style={{
                backgroundColor: colors.accent,
                borderColor: colors.accent
              }}
            >
              <User className="h-4 w-4 mr-3" />
              Login
            </Button>
          )}
        </div>
      </motion.div>
    </>
  );
};

export default Sidebar;