// ====================================
// HEADER COMPONENT
// ====================================

import { useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Button } from './ui/button'
import { useTheme } from '../contexts/ThemeContext.jsx'
import { useAuth } from '../contexts/AuthContext.jsx'
import {
    Calculator,
    Menu,
    Sun,
    Moon,
    LogOut
} from 'lucide-react'

export function Header({ onMenuClick, showUserInfo = true }) {
    const { colors, toggleTheme, isDark } = useTheme()
    const { isAuthenticated, user, logout } = useAuth()
    const navigate = useNavigate()

    return (
        <motion.header
            initial={{ y: -100, opacity: 0 }}
            animate={{ y: 0, opacity: 1 }}
            transition={{ duration: 0.6 }}
            className="backdrop-blur-sm border-b sticky top-0 z-50"
            style={{
                backgroundColor: `${colors.surface}40`,
                borderColor: colors.border
            }}
        >
            <div className="w-full px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between items-center py-4">
                    {/* Logo and navigation buttons */}
                    <div className="flex items-center space-x-4">
                        {/* Menu button */}
                        <Button
                            variant="ghost"
                            size="sm"
                            onClick={onMenuClick}
                            className="transition-all duration-300 hover:bg-opacity-20"
                            style={{
                                color: colors.foreground,
                                backgroundColor: `${colors.surface}90`
                            }}
                            
                        >
                            <Menu className="h-5 w-5" />
                        </Button>

                        <motion.div
                            className="flex items-center space-x-3"
                            whileHover={{ scale: 1.05 }}
                        >
                            <div
                                className="p-2 rounded-lg shadow-lg"
                                style={{ backgroundColor: colors.accent }}
                            >
                                <img
                                    src="/tempGOAT.png"
                                    alt="GoatTariff Logo"
                                    className="h-12 w-12 object-contain"
                                />
                            </div>
                            <span className="text-xl font-bold" style={{ color: colors.foreground }}>
                                GoatTariff
                            </span>
                        </motion.div>
                    </div>

                    {/* Right side - User/Login + Theme toggle */}
                    <div className="flex items-center space-x-3">
                        {showUserInfo && isAuthenticated ? (
                            <div className="flex items-center space-x-3">
                                <div className="hidden sm:flex items-center space-x-2 px-3 py-1 rounded-lg text-sm"
                                     style={{ backgroundColor: colors.surface, border: `1px solid ${colors.border}` }}>
                                    <div className="w-6 h-6 rounded-full bg-gradient-to-r from-blue-500 to-purple-600 flex items-center justify-center">
                                        <span className="text-white text-xs font-semibold">
                                            {user?.email?.charAt(0).toUpperCase() || 'U'}
                                        </span>
                                    </div>
                                    <span style={{ color: colors.foreground }}>{user?.email || 'User'}</span>
                                </div>
                                <Button
                                    onClick={() => {
                                        logout();
                                        navigate('/');
                                    }}
                                    className="transition-all duration-300 text-white"
                                    style={{
                                        backgroundColor: '#dc2626',
                                        borderColor: '#dc2626'
                                    }}
                                    size="sm"
                                >
                                    <LogOut className="h-4 w-4 mr-2" />
                                    Logout
                                </Button>
                            </div>
                        ) : showUserInfo && !isAuthenticated ? (
                            <Button
                                onClick={() => navigate('/login')}
                                className="transition-all duration-300 text-white"
                                style={{
                                    backgroundColor: colors.accent,
                                    borderColor: colors.accent
                                }}
                                size="sm"
                            >
                                Login
                            </Button>
                        ) : null}

                        <motion.div
                            whileHover={{ scale: 1.05 }}
                            whileTap={{ scale: 0.95 }}
                        >
                            <Button
                                variant="outline"
                                size="sm"
                                onClick={toggleTheme}
                                className="transition-all duration-300 shadow-md"
                                style={{
                                    borderColor: colors.foreground,
                                    backgroundColor: isDark ? '#374151' : '#f3f4f6',
                                    color: colors.accent,
                                    borderWidth: '0px'
                                }}
                                onMouseEnter={(e) => {
                                    e.target.style.backgroundColor = colors.accent;
                                    e.target.style.color = 'white';
                                }}
                                onMouseLeave={(e) => {
                                    e.target.style.backgroundColor = isDark ? '#374151' : '#f3f4f6';
                                    e.target.style.color = colors.accent;
                                }}
                            >
                                {isDark ? (
                                    <Sun className="h-4 w-4" />
                                ) : (
                                    <Moon className="h-4 w-4" />
                                )}
                            </Button>
                        </motion.div>
                    </div>
                </div>
            </div>
        </motion.header>
    )
}