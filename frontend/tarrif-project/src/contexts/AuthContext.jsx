import { createContext, useContext, useState, useEffect } from 'react';

/**
 * Authentication context for managing user authentication state
 *
 * Features:
 * - Authentication state management (boolean)
 * - Login/logout functionality
 * - Token persistence in localStorage
 * - Automatic authentication check on app load
 * - User data management
 */

const AuthContext = createContext({
    isAuthenticated: false,
    user: null,
    login: () => {},
    logout: () => {},
    loading: true
});

/**
 * Custom hook to access authentication context
 *
 * @returns {Object} Auth context with authentication state and functions
 */
export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

/**
 * Authentication provider component that wraps the application
 *
 * Features:
 * - Manages authentication state
 * - Handles login/logout operations
 * - Persists authentication token
 * - Provides authentication state to all components
 *
 * @param {Object} props - Component props
 * @param {ReactNode} props.children - Child components
 */
export const AuthProvider = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    // Check for existing authentication on app load
    useEffect(() => {
        const checkAuth = () => {
            const token = localStorage.getItem('authToken');
            const storedUser = localStorage.getItem('userData');
            if (token) {
                // Here you could validate the token with your backend
                // For now, we'll just check if it exists
                setIsAuthenticated(true);
                if (storedUser) {
                    setUser(JSON.parse(storedUser));
                }
            }
            setLoading(false);
        };

        checkAuth();
    }, []);

    /**
     * Login function
     * @param {Object} userData - User data from login response
     */
    const login = (userData) => {
        setIsAuthenticated(true);
        setUser(userData);
        localStorage.setItem('userData', JSON.stringify(userData));
        // Token is already stored in Login component
    };

    /**
     * Logout function
     */
    const logout = () => {
        setIsAuthenticated(false);
        setUser(null);
        localStorage.removeItem('authToken');
        localStorage.removeItem('userData');
        // You might want to call logout API endpoint here
    };

    const value = {
        isAuthenticated,
        user,
        login,
        logout,
        loading
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};