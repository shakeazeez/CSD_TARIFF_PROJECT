import { useContext } from 'react';
import { AuthContext } from './AuthContext';

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