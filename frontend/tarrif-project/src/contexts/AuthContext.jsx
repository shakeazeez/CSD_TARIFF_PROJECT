import { createContext, useState, useEffect } from 'react';

/**
 * Authentication context for managing user authentication state
 *
 * Features:
 * - Authentication state management (boolean)
 * - Login/logout functionality
 * - Token persistence in sessionStorage
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
            const token = sessionStorage.getItem('authToken');
            const storedUser = sessionStorage.getItem('userData');
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
    const login = (authPayload = {}) => {
        const { token, username, pin, ...rest } = authPayload || {};

        const normalizedUsername = username ?? rest?.username ?? null;

        let normalizedPin = [];
        if (Array.isArray(pin)) {
            normalizedPin = pin;
        } else if (typeof pin === 'string' && pin.length > 0) {
            normalizedPin = pin
                .split(',')
                .map((value) => {
                    const parsed = Number(value.trim());
                    return Number.isNaN(parsed) ? null : parsed;
                })
                .filter((value) => value !== null);
        } else if (pin !== undefined && pin !== null) {
            normalizedPin = [pin];
        }

        const userData = {
            ...rest,
            username: normalizedUsername,
            pin: normalizedPin
        };

        setIsAuthenticated(true);
        setUser(userData);

        sessionStorage.setItem('userData', JSON.stringify(userData));

        if (token) {
            sessionStorage.setItem('authToken', token);
        }

        if (normalizedUsername) {
            sessionStorage.setItem('username', normalizedUsername);
        }

        if (normalizedPin.length > 0 || Array.isArray(pin)) {
            sessionStorage.setItem('pin', JSON.stringify(normalizedPin));
        } else {
            sessionStorage.setItem('pin', JSON.stringify([]));
        }
    };

    /**
     * Logout function
     */
    const logout = () => {
        setIsAuthenticated(false);
        setUser(null);
        sessionStorage.removeItem('authToken');
        sessionStorage.removeItem('userData');
        sessionStorage.removeItem('username');
        sessionStorage.removeItem('token');
        sessionStorage.removeItem('pin');
        sessionStorage.removeItem('generalUserTopSearches');
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

export { AuthContext };
export default AuthContext;