// ====================================
// IMPORTS SECTION
// ====================================

// External libraries
import axios from 'axios' // HTTP client for API calls
import { useEffect, useState } from 'react' // React hooks for state management and side effects
import { useNavigate } from 'react-router-dom' // Navigation hook

// Animation library for smooth transitions
import { motion, AnimatePresence } from 'framer-motion'

// shadcn/ui components - Modern, accessible UI components
import { Button } from '../components/ui/button' // Customizable button component
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card' // Card layout components
import { Input } from '../components/ui/input' // Styled input component
import { Label } from '../components/ui/label' // Form label component

// Theme and icon components
import { useTheme } from '../contexts/ThemeContext.jsx' // Custom theme context for component-level theming
import { useAuth } from '../contexts/AuthContext.jsx' // Authentication context for user management
import {
    Sun,
    Moon,
    LogIn,
    User,
    Lock,
    Eye,
    EyeOff,
    Sparkles,
    Shield,
    CheckCircle,
    AlertCircle,
    Loader2,
    ArrowRight
} from 'lucide-react' // SVG icons

// ====================================
// ANIMATION VARIANTS
// ====================================

const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
        opacity: 1,
        transition: {
            duration: 0.6,
            staggerChildren: 0.1
        }
    }
}

const itemVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: {
        opacity: 1,
        y: 0,
        transition: {
            duration: 0.5,
            ease: "easeOut"
        }
    }
}

const cardVariants = {
    hidden: { opacity: 0, scale: 0.95 },
    visible: {
        opacity: 1,
        scale: 1,
        transition: {
            duration: 0.4,
            ease: "easeOut"
        }
    }
}

// ====================================
// LOGIN COMPONENT
// ====================================

export function Login(){
    // ====================================
    // THEME INTEGRATION
    // ====================================

    // Get theme context for component-level color management
    const { colors, toggleTheme, isDark } = useTheme();

    // Get authentication context
    const { login } = useAuth();

    // ====================================
    // NAVIGATION
    // ====================================

    const navigate = useNavigate();

    // ====================================
    // STATE VARIABLES
    // ====================================

    // Get backend URL from environment variables (.env file)
    const backendURL = import.meta.env.VITE_BACKEND_URL;

    // Login form data
    const [email, setEmail] = useState(""); // User email
    const [password, setPassword] = useState(""); // User password
    const [showPassword, setShowPassword] = useState(false); // Password visibility toggle
    const [isSignUp, setIsSignUp] = useState(false); // Toggle between login and sign up

    // UI state
    const [isLoading, setIsLoading] = useState(false); // Loading state for login/signup
    const [error, setError] = useState(""); // Error messages
    const [success, setSuccess] = useState(""); // Success messages

    // ====================================
    // USER DTO
    // ====================================

    // DTO for login/signup API call
    const authDTO = {
        email: email,
        password: password
    }

    // ====================================
    // UTILITY FUNCTIONS
    // ====================================

    // Clear messages after timeout
    const clearMessages = () => {
        setTimeout(() => {
            setError("");
            setSuccess("");
        }, 5000);
    };

    // Validate form inputs
    const validateInputs = () => {
        if (!email) return "Please enter your email address";
        if (!password) return "Please enter your password";
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) return "Please enter a valid email address";
        if (password.length < 6) return "Password must be at least 6 characters long";
        return null;
    };

    // ====================================
    // AUTHENTICATION FUNCTIONS
    // ====================================

    // Function to handle user authentication (login/signup)
    const handleAuth = async() => {
        const validationError = validateInputs();
        if (validationError) {
            setError(validationError);
            clearMessages();
            return;
        }

        setIsLoading(true);
        setError("");
        setSuccess("");

        try{
            // DEMO CREDENTIALS - Remove in production
            const DEMO_EMAIL = "demo@tariff.com";
            const DEMO_PASSWORD = "demo123";

            // Check for demo credentials first
            if (email === DEMO_EMAIL && password === DEMO_PASSWORD) {
                console.log("Demo login successful");

                // Simulate successful authentication for demo
                const action = isSignUp ? 'Account created' : 'Login successful';
                setSuccess(`${action}! Redirecting...`);
                clearMessages();

                // Store demo authentication token
                const demoToken = "demo-token-" + Date.now();
                localStorage.setItem('authToken', demoToken);

                // Set demo user data
                const demoUser = {
                    email: DEMO_EMAIL,
                    name: "Demo User",
                    role: "Premium User"
                };

                // Update auth context
                login(demoUser);

                // Redirect to dashboard page after successful authentication
                setTimeout(() => {
                    navigate('/dashboard');
                }, 1500);

                setIsLoading(false);
                return;
            }

            // Log the data being sent for debugging
            console.log(`Sending ${isSignUp ? 'signup' : 'login'} DTO:`, authDTO);

            // POST request to appropriate endpoint
            const endpoint = isSignUp ? '/auth/signup' : '/auth/login';
            const response = await axios.post(`${backendURL}${endpoint}`, authDTO);
            console.log(`${isSignUp ? 'Signup' : 'Login'} success:`, response);

            // Handle successful authentication
            const action = isSignUp ? 'Account created' : 'Login successful';
            setSuccess(`${action}! Redirecting...`);
            clearMessages();

            // Store authentication token (you can modify this based on your API response)
            if (response.data.token) {
                localStorage.setItem('authToken', response.data.token);
            }

            // Update auth context with user data
            login(response.data.user || { email: email });

            // Redirect to dashboard page after successful authentication
            setTimeout(() => {
                navigate('/dashboard');
            }, 1500);

        } catch(error){
            console.log(`${isSignUp ? 'Signup' : 'Login'} error:`, error);
            const action = isSignUp ? 'signup' : 'login';
            setError(error.response?.data?.message || `${action.charAt(0).toUpperCase() + action.slice(1)} failed. Please try again.`);
            clearMessages();
        } finally {
            setIsLoading(false);
        }
    };

    // Handle form submission
    const handleSubmit = (e) => {
        e.preventDefault();
        handleAuth();
    };

    // ====================================
    // COMPONENT RENDER (JSX)
    // ====================================

    return(
        /* Main container - smaller and centered */
        <motion.div
            initial="hidden"
            animate="visible"
            variants={containerVariants}
            className="min-h-screen flex items-center justify-center p-4 transition-colors duration-300"
            style={{
                background: 'transparent'
            }}
        >

            {/* Back button */}
            <motion.div
                initial={{ x: -100, opacity: 0 }}
                animate={{ x: 0, opacity: 1 }}
                transition={{ duration: 0.6 }}
                className="fixed top-4 left-4 z-50"
            >
                <Button
                    variant="outline"
                    size="sm"
                    onClick={() => navigate('/')}
                    className="transition-all duration-300 hover:scale-105 shadow-md"
                    style={{
                        borderColor: colors.accent,
                        backgroundColor: colors.surface,
                        color: colors.accent,
                        borderWidth: '1px'
                    }}
                    onMouseEnter={(e) => {
                        e.target.style.backgroundColor = colors.accent;
                        e.target.style.color = 'white';
                    }}
                    onMouseLeave={(e) => {
                        e.target.style.backgroundColor = colors.surface;
                        e.target.style.color = colors.accent;
                    }}
                >
                    ← Back
                </Button>
            </motion.div>
            {/* Theme toggle */}
            <motion.div
                initial={{ y: -100, opacity: 0 }}
                animate={{ y: 0, opacity: 1 }}
                transition={{ duration: 0.6, delay: 0.2 }}
                className="fixed top-4 right-4 z-50"
            >
                <Button
                    variant="outline"
                    size="sm"
                    onClick={toggleTheme}
                    className="transition-all duration-300 hover:scale-105 shadow-md"
                    style={{
                        borderColor: colors.accent,
                        backgroundColor: colors.surface,
                        color: colors.accent,
                        borderWidth: '1px'
                    }}
                    onMouseEnter={(e) => {
                        e.target.style.backgroundColor = colors.accent;
                        e.target.style.color = 'white';
                    }}
                    onMouseLeave={(e) => {
                        e.target.style.backgroundColor = colors.surface;
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

            {/* SUCCESS/ERROR MESSAGES */}
            <AnimatePresence>
                {success && (
                    <motion.div
                        initial={{ opacity: 0, y: -50 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: -50 }}
                        className="fixed top-20 right-4 z-50"
                    >
                        <Card className="border-green-500 bg-green-50 dark:bg-green-900/20 shadow-lg">
                            <CardContent className="flex items-center space-x-2 p-4">
                                <CheckCircle className="h-5 w-5 text-green-600" />
                                <span className="text-green-800 dark:text-green-200">{success}</span>
                            </CardContent>
                        </Card>
                    </motion.div>
                )}

                {error && (
                    <motion.div
                        initial={{ opacity: 0, y: -50 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: -50 }}
                        className="fixed top-20 right-4 z-50"
                    >
                        <Card className="border-red-500 bg-red-50 dark:bg-red-900/20 shadow-lg">
                            <CardContent className="flex items-center space-x-2 p-4">
                                <AlertCircle className="h-5 w-5 text-red-600" />
                                <span className="text-red-800 dark:text-red-200">{error}</span>
                            </CardContent>
                        </Card>
                    </motion.div>
                )}
            </AnimatePresence>

            {/* MAIN AUTH CARD */}
            <motion.div
                variants={cardVariants}
                initial="hidden"
                animate="visible"
                className="w-full max-w-sm"
            >
                <Card
                    className="shadow-xl border-0 backdrop-blur-sm transition-colors duration-300"
                    style={{ backgroundColor: `${colors.surface}cc` }}
                >
                    <CardHeader className="text-center pb-4">
                        {/* Animated logo/icon */}
                        <motion.div
                            
                            transition={{
                                duration: 4,
                                repeat: Infinity,
                                repeatType: "reverse"
                            }}
                            className="flex justify-center mb-3"
                        >
                            <div
                                className="p-3 rounded-xl shadow-lg transition-colors duration-300"
                                style={{ backgroundColor: colors.accent }}
                            >
                                <Shield className="h-8 w-8 text-white" />
                            </div>
                        </motion.div>

                        {/* Title and description */}
                        <motion.div variants={itemVariants}>
                            <CardTitle
                                className="text-xl sm:text-2xl font-bold mb-2 transition-colors duration-300"
                                style={{ color: colors.foreground }}
                            >
                                {isSignUp ? 'Create Account' : 'Welcome Back'}
                            </CardTitle>
                            <CardDescription
                                className="text-sm transition-colors duration-300"
                                style={{ color: colors.muted }}
                            >
                                {isSignUp ? 'Sign up to access tariff analysis' : 'Sign in to your account'}
                            </CardDescription>
                        </motion.div>
                    </CardHeader>

                    <CardContent>
                        <form onSubmit={handleSubmit} className="space-y-6">
                            {/* EMAIL INPUT */}
                            <motion.div
                                variants={itemVariants}
                                className="space-y-2"
                            >
                                <Label
                                    htmlFor="email"
                                    className="text-sm font-medium flex items-center space-x-1 transition-colors duration-300"
                                    style={{ color: colors.foreground }}
                                >
                                    <User className="h-4 w-4" />
                                    <span>Email Address</span>
                                </Label>
                                <Input
                                    type="email"
                                    id="email"
                                    placeholder="Enter your email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    className="transition-colors duration-300"
                                    style={{
                                        backgroundColor: colors.input,
                                        borderColor: colors.border,
                                        color: colors.foreground
                                    }}
                                    disabled={isLoading}
                                />
                            </motion.div>

                            {/* PASSWORD INPUT */}
                            <motion.div
                                variants={itemVariants}
                                className="space-y-2"
                            >
                                <Label
                                    htmlFor="password"
                                    className="text-sm font-medium flex items-center space-x-1 transition-colors duration-300"
                                    style={{ color: colors.foreground }}
                                >
                                    <Lock className="h-4 w-4" />
                                    <span>Password</span>
                                </Label>
                                <div className="relative">
                                    <Input
                                        type={showPassword ? "text" : "password"}
                                        id="password"
                                        placeholder="Enter your password"
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        className="transition-colors duration-300 pr-10"
                                        style={{
                                            backgroundColor: colors.input,
                                            borderColor: colors.border,
                                            color: colors.foreground
                                        }}
                                        disabled={isLoading}
                                    />
                                    <Button
                                        type="button"
                                        variant="ghost"
                                        size="sm"
                                        className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                                        onClick={() => setShowPassword(!showPassword)}
                                        disabled={isLoading}
                                    >
                                        {showPassword ? (
                                            <EyeOff className="h-4 w-4" style={{ color: colors.muted }} />
                                        ) : (
                                            <Eye className="h-4 w-4" style={{ color: colors.muted }} />
                                        )}
                                    </Button>
                                </div>
                            </motion.div>

                            {/* AUTH BUTTON */}
                            <motion.div
                                variants={itemVariants}
                                className="pt-2"
                            >
                                <motion.div whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
                                    <Button
                                        type="submit"
                                        disabled={isLoading}
                                        className="w-full text-white transition-all duration-300 shadow-lg"
                                        style={{
                                            backgroundColor: colors.accent,
                                            borderColor: colors.accent
                                        }}
                                        onMouseEnter={(e) => e.target.style.backgroundColor = colors.hover}
                                        onMouseLeave={(e) => e.target.style.backgroundColor = colors.accent}
                                        size="lg"
                                    >
                                        {isLoading ? (
                                            <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                                        ) : (
                                            <LogIn className="h-4 w-4 mr-2" />
                                        )}
                                        {isLoading ? (isSignUp ? 'Creating Account...' : 'Signing In...') : (isSignUp ? 'Sign Up' : 'Sign In')}
                                    </Button>
                                </motion.div>
                            </motion.div>

                            {/* TOGGLE BETWEEN LOGIN/SIGNUP */}
                            <motion.div
                                variants={itemVariants}
                                className="text-center pt-3"
                            >
                                <Button
                                    type="button"
                                    variant="link"
                                    onClick={() => {
                                        setIsSignUp(!isSignUp);
                                        setError("");
                                        setSuccess("");
                                        setEmail("");
                                        setPassword("");
                                    }}
                                    className="text-sm transition-colors duration-300"
                                    style={{ color: colors.accent }}
                                    disabled={isLoading}
                                >
                                    {isSignUp ? 'Already have an account? Sign In' : "Don't have an account? Sign Up"}
                                </Button>
                            </motion.div>

                            {/* DEMO CREDENTIALS */}
                            {!isSignUp && (
                                <motion.div
                                    variants={itemVariants}
                                    className="text-center pt-3 border-t"
                                    style={{ borderColor: colors.border }}
                                >
                                    <p
                                        className="text-xs mb-2 transition-colors duration-300"
                                        style={{ color: colors.muted }}
                                    >
                                        Demo Credentials:
                                    </p>
                                    <div className="text-xs space-y-1">
                                        <p style={{ color: colors.muted }}>Email: demo@tariff.com</p>
                                        <p style={{ color: colors.muted }}>Password: demo123</p>
                                    </div>
                                </motion.div>
                            )}
                        </form>
                    </CardContent>
                </Card>
            </motion.div>

            {/* BACKGROUND DECORATION */}
            <div className="fixed inset-0 overflow-hidden pointer-events-none">
                <motion.div
                    animate={{
                        scale: [1, 1.2, 1],
                        opacity: [0.1, 0.2, 0.1]
                    }}
                    transition={{
                        duration: 8,
                        repeat: Infinity,
                        repeatType: "reverse"
                    }}
                    className="absolute top-1/4 left-1/4 w-64 h-64 rounded-full blur-3xl"
                    style={{ backgroundColor: colors.accent }}
                />
                <motion.div
                    animate={{
                        scale: [1.2, 1, 1.2],
                        opacity: [0.1, 0.15, 0.1]
                    }}
                    transition={{
                        duration: 6,
                        repeat: Infinity,
                        repeatType: "reverse"
                    }}
                    className="absolute bottom-1/4 right-1/4 w-48 h-48 rounded-full blur-3xl"
                    style={{ backgroundColor: colors.accent }}
                />
            </div>
        </motion.div>
    );
}