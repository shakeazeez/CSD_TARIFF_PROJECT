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
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select' // Select component

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
    ArrowRight,
    X
} from 'lucide-react' // SVG icons

import { useToast } from '../hooks/use-toast'

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
    const { isAuthenticated, login } = useAuth();

    // Toast hook
    const { toast } = useToast();

    // ====================================
    // NAVIGATION
    // ====================================

    const navigate = useNavigate();

    // Redirect if already authenticated
    useEffect(() => {
        if (isAuthenticated) {
            navigate('/dashboard');
        }
    }, [isAuthenticated, navigate]);

    // ====================================
    // STATE VARIABLES
    // ====================================

    // Get backend URL from environment variables (.env file)
    const backendURL = import.meta.env.VITE_BACKEND_URL;

    // Login form data
    const [form, setForm] = useState({ username: "", password: "", rePassword: "" }); // Form data
    const [role, setRole] = useState(""); // User role for signup
    const [showPassword, setShowPassword] = useState(false); // Password visibility toggle
    const [showRePassword, setShowRePassword] = useState(false); // Re-enter password visibility toggle
    const [isSignUp, setIsSignUp] = useState(false); // Toggle between login and sign up

    // UI state
    const [isLoading, setIsLoading] = useState(false); // Loading state for login/signup
    const [error, setError] = useState({username: "", password: "", rePassword: ""}); // Error messages for fields
    const [allow, setAllow] = useState(""); // General error message
    const [success, setSuccess] = useState(""); // Success messages
    const [passwordReqs, setPasswordReqs] = useState({
        length: false,
        number: false,
        special: false
    }); // Password requirements status

    // ====================================
    // EFFECTS
    // ====================================

    // Auto-dismiss error message after 5 seconds
    useEffect(() => {
        if (allow) {
            const timer = setTimeout(() => setAllow(''), 5000);
            return () => clearTimeout(timer);
        }
    }, [allow]);

    // Auto-dismiss success message after 5 seconds
    useEffect(() => {
        if (success) {
            const timer = setTimeout(() => setSuccess(''), 5000);
            return () => clearTimeout(timer);
        }
    }, [success]);

    // ====================================
    // USER DTO
    // ====================================

    // DTO for login/signup API call
    // DTO for login/signup API call
    const authDTO = {
        username: form.username,
        password: form.password,
        role: role
    }

    // ====================================
    // UTILITY FUNCTIONS
    // ====================================

    // Handle form input changes
    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm({...form, [name]: value});
        
        // Update password requirements in real-time
        if (name === 'password') {
            setPasswordReqs({
                length: value.length >= 8 && value.length <= 20,
                number: /(?=.*\d)/.test(value),
                special: /(?=.*[#@$!%*?&])/.test(value)
            });
        }
    };

    // Validate form inputs
    const validateInputs = () => {
        if (!form.username) return "Please enter your username";
        if (!form.password) return "Please enter your password";
        if (form.password.length < 8) return "Password must be at least 8 characters long";
        if (form.password.length > 20) return "Password must be at most 20 characters long";
        if (isSignUp && !role) return "Please select a role";
        if (isSignUp && form.password !== form.rePassword) return "Passwords do not match";
        return null;
    };

    /*
     *  Method performs basic validation for username and password. 
     */
    function validateForm (form) {
        let tempErrors = ({username: "", password: "", rePassword: ""});

        const usernameValidChars = /^[a-zA-Z0-9._]+$/;
        const passwordValidChars = /^[a-zA-Z0-9#$]+$/;

        if (form.username == "" || form.password == "") {
            tempErrors.username = "Username is required";
            tempErrors.password = "Password is required";  

        } else if (form.username.includes(" ")) {
            tempErrors.username = "Username cannot contain spaces";            

        } else if (!usernameValidChars.test(form.username)) {
            tempErrors.username = "Username can only contain letters, numbers, dots, and underscores";
 
        } else if (form.password.length < 8 || form.password.length > 20) {
            tempErrors.password = "Password must be between 8 and 20 characters";   

        } else if (!/(?=.*\d)/.test(form.password)) {
            tempErrors.password = "Password must contain at least one number";

        } else if (!/(?=.*[#@$!%*?&])/.test(form.password)) {
            tempErrors.password = "Password must contain at least one special character (#@$!%*?&)";

        } else if (isSignUp && form.password !== form.rePassword) {
            tempErrors.rePassword = "Passwords do not match";
        }

        setError(tempErrors);

        return Object.values(tempErrors).every((msg) => (msg) == "");

    }

    // ====================================
    // AUTHENTICATION FUNCTIONS
    // ====================================

    // Function to handle user authentication (login/signup)
    const handleAuth = async() => {
        const validationError = validateInputs();
        if (validationError) {
            setAllow(validationError);
            return;
        }

        setIsLoading(true);
        setAllow("");
        setSuccess("");

        try{
            // DEMO CREDENTIALS - Remove in production
            const DEMO_USERNAME = "demo_user";
            const DEMO_PASSWORD = "DemoPass123!";

            // Check for demo credentials first
            if (form.username === DEMO_USERNAME && form.password === DEMO_PASSWORD) {
                // Store demo authentication token
                const demoToken = "demo-token-" + Date.now();
                localStorage.setItem('authToken', demoToken);

                // Set demo user data
                const demoUser = {
                    username: DEMO_USERNAME,
                    name: "Demo User",
                    role: "member"
                };

                // Update auth context
                login(demoUser);

                // Redirect to dashboard page after successful authentication
                navigate('/dashboard');

                setIsLoading(false);
                return;
            }

            // Prepare form data for backend
            const data = {
                username: form.username,
                password: form.password,
                ...(isSignUp && { role })
            };

            // POST request to appropriate endpoint
            const endpoint = isSignUp ? '/auth/register' : '/auth/login';
            const response = await axios.post(`${backendURL}${endpoint}`, data, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            // Handle successful authentication
            // Store authentication token (you can modify this based on your API response)
            if (response.data.token) {
                localStorage.setItem('authToken', response.data.token);
            }

            // Store user data for both login and signup (since signup now returns TokenDTO)
            localStorage.setItem("username", response.data.username);
            // localStorage.setItem('userId', response.data.userId);
            localStorage.setItem("token", response.data.token);
            localStorage.setItem('pin', JSON.stringify(response.data.pin)); // Pinned tariffs

            // Update auth context with user data
            login({ username: response.data.username });

            // Redirect to dashboard for both login and signup (since signup now auto-logs in)
            navigate('/dashboard');

        } catch(error){
            console.error(`${isSignUp ? 'Signup' : 'Login'} error:`, error);
            const action = isSignUp ? 'signup' : 'login';
            const errorMessage = error.response?.status === 401 ? "Username or password is invalid." : (error.response?.data?.message || `${action.charAt(0).toUpperCase() + action.slice(1)} failed. Please try again.`);
            setAllow(errorMessage);
        } finally {
            setIsLoading(false);
        }
    };

    // Handle form submission
    const handleSubmit = async(e) => {
        e.preventDefault(); // prevent the page from reloading

        if (!isSignUp) {
            if (!validateForm(form)) {
                console.error("Validation failed:", error);
                return;
            }
        }

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
                        e.target.style.transform = 'scale(1.05)';
                    }}
                    onMouseLeave={(e) => {
                        e.target.style.backgroundColor = colors.surface;
                        e.target.style.color = colors.accent;
                        e.target.style.transform = 'scale(1)';
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
                        e.target.style.transform = 'scale(1.05)';
                    }}
                    onMouseLeave={(e) => {
                        e.target.style.backgroundColor = colors.surface;
                        e.target.style.color = colors.accent;
                        e.target.style.transform = 'scale(1)';
                    }}
                >
                    {isDark ? (
                        <Sun className="h-4 w-4" />
                    ) : (
                        <Moon className="h-4 w-4" />
                    )}
                </Button>
            </motion.div>

            {/* NOTIFICATIONS */}
            <AnimatePresence>
                {success && (
                    <motion.div
                        initial={{ opacity: 0, y: -50 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: -50 }}
                        className="fixed top-20 right-4 z-50"
                    >
                        <Card 
                            className="shadow-lg"
                            style={{
                                borderColor: colors.success,
                                backgroundColor: `${colors.success}20`,
                                borderWidth: '1px'
                            }}
                        >
                            <CardContent className="flex items-center space-x-2 p-4">
                                <CheckCircle 
                                    className="h-5 w-5" 
                                    style={{ color: colors.success }} 
                                />
                                <span style={{ color: colors.foreground }}>
                                    {success}
                                </span>
                            </CardContent>
                        </Card>
                    </motion.div>
                )}

                {allow && (
                    <motion.div
                        initial={{ opacity: 0, y: -50 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, y: -50 }}
                        className="fixed top-20 right-4 z-50"
                    >
                        <Card 
                            className="shadow-lg"
                            style={{
                                borderColor: colors.error,
                                backgroundColor: `${colors.error}20`,
                                borderWidth: '1px'
                            }}
                        >
                            <CardContent className="flex items-center space-x-2 p-4">
                                <AlertCircle 
                                    className="h-5 w-5" 
                                    style={{ color: colors.error }} 
                                />
                                <span style={{ color: colors.foreground }}>
                                    {allow}
                                </span>
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
                            {/* USERNAME INPUT */}
                            <motion.div
                                variants={itemVariants}
                                className="space-y-2"
                            >
                                <Label
                                    htmlFor="username"
                                    className="text-sm font-medium flex items-center space-x-1 transition-colors duration-300"
                                    style={{ color: colors.foreground }}
                                >
                                    <User className="h-4 w-4" />
                                    <span>Username</span>
                                </Label>
                                <Input
                                    type="text"
                                    id="username"
                                    name="username"
                                    placeholder="Enter your username"
                                    value={form.username}
                                    onChange={handleChange}
                                    className="transition-colors duration-300"
                                    style={{
                                        backgroundColor: colors.input,
                                        borderColor: colors.border,
                                        color: colors.foreground
                                    }}
                                    disabled={isLoading}
                                />
                                {error.username && <p className='error-message-username' style={{ color: colors.error }}>{error.username}</p>}
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
                                        name="password"
                                        placeholder="Enter your password"
                                        value={form.password}
                                        onChange={handleChange}
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
                                        style={{
                                            backgroundColor: 'transparent',
                                            color: colors.foreground,
                                            border: 'none'
                                        }}
                                        onMouseEnter={(e) => {
                                            e.target.style.backgroundColor = colors.hover || colors.accent + '20';
                                            
                                        }}
                                        onMouseLeave={(e) => {
                                            e.target.style.backgroundColor = 'transparent';
                                            
                                        }}
                                    >
                                        {showPassword ? (
                                            <EyeOff className="h-4 w-4" style={{ color: colors.muted }} />
                                        ) : (
                                            <Eye className="h-4 w-4" style={{ color: colors.muted }} />
                                        )}
                                    </Button>
                                </div>
                                {error.password && <p className='error-message-password' style={{ color: colors.error }}>{error.password}</p>}
                                {isSignUp && !error.password && (
                                    <div className='password-requirements' style={{ color: colors.muted, fontSize: '0.75rem', marginTop: '0.5rem' }}>
                                        <div style={{ display: 'flex', alignItems: 'center', marginBottom: '0.25rem' }}>
                                            {passwordReqs.length ? (
                                                <CheckCircle className="h-3 w-3" style={{ color: colors.success, marginRight: '0.5rem' }} />
                                            ) : (
                                                <X className="h-3 w-3" style={{ color: colors.error, marginRight: '0.5rem' }} />
                                            )}
                                            <span>8-20 characters</span>
                                        </div>
                                        <div style={{ display: 'flex', alignItems: 'center', marginBottom: '0.25rem' }}>
                                            {passwordReqs.number ? (
                                                <CheckCircle className="h-3 w-3" style={{ color: colors.success, marginRight: '0.5rem' }} />
                                            ) : (
                                                <X className="h-3 w-3" style={{ color: colors.error, marginRight: '0.5rem' }} />
                                            )}
                                            <span>At least one number</span>
                                        </div>
                                        <div style={{ display: 'flex', alignItems: 'center' }}>
                                            {passwordReqs.special ? (
                                                <CheckCircle className="h-3 w-3" style={{ color: colors.success, marginRight: '0.5rem' }} />
                                            ) : (
                                                <X className="h-3 w-3" style={{ color: colors.error, marginRight: '0.5rem' }} />
                                            )}
                                            <span>At least one special character (#@$!%*?&)</span>
                                        </div>
                                    </div>
                                )}
                            </motion.div>

                            {/* RE-ENTER PASSWORD INPUT - Only for signup */}
                            {isSignUp && (
                                <motion.div
                                    variants={itemVariants}
                                    className="space-y-2"
                                >
                                    <Label
                                        htmlFor="rePassword"
                                        className="text-sm font-medium flex items-center space-x-1 transition-colors duration-300"
                                        style={{ color: colors.foreground }}
                                    >
                                        <Lock className="h-4 w-4" />
                                        <span>Re-enter Password</span>
                                    </Label>
                                    <div className="relative">
                                        <Input
                                            type={showRePassword ? "text" : "password"}
                                            id="rePassword"
                                            name="rePassword"
                                            placeholder="Re-enter your password"
                                            value={form.rePassword}
                                            onChange={handleChange}
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
                                            onClick={() => setShowRePassword(!showRePassword)}
                                            disabled={isLoading}
                                            style={{
                                                backgroundColor: 'transparent',
                                                color: colors.foreground,
                                                border: 'none'
                                            }}
                                            onMouseEnter={(e) => {
                                                e.target.style.backgroundColor = colors.hover || colors.accent + '20';
                                                
                                            }}
                                            onMouseLeave={(e) => {
                                                e.target.style.backgroundColor = 'transparent';
                                                
                                            }}
                                        >
                                            {showRePassword ? (
                                                <EyeOff className="h-4 w-4" style={{ color: colors.muted }} />
                                            ) : (
                                                <Eye className="h-4 w-4" style={{ color: colors.muted }} />
                                            )}
                                        </Button>
                                    </div>
                                    {error.rePassword && <p className='error-message-repassword' style={{ color: colors.error }}>{error.rePassword}</p>}
                                </motion.div>
                            )}

                            {/* USER TYPE SELECT - Only for signup */}
                            {isSignUp && (
                                <motion.div
                                    variants={itemVariants}
                                    className="space-y-2"
                                >
                                    <Label
                                        htmlFor="role"
                                        className="text-sm font-medium flex items-center space-x-1 transition-colors duration-300"
                                        style={{ color: colors.foreground }}
                                    >
                                        <User className="h-4 w-4" />
                                        <span>Role</span>
                                    </Label>
                                    <Select value={role} onValueChange={setRole} disabled={isLoading}>
                                        <SelectTrigger
                                            className="transition-colors duration-300"
                                            style={{
                                                backgroundColor: colors.input,
                                                borderColor: colors.border,
                                                color: colors.foreground
                                            }}
                                        >
                                            <SelectValue placeholder="Select user type" />
                                        </SelectTrigger>
                                        <SelectContent
                                            style={{
                                                backgroundColor: colors.surface,
                                                borderColor: colors.border
                                            }}
                                        >
                                            <SelectItem value="Member" style={{ color: colors.foreground }}>Member</SelectItem>
                                            <SelectItem value="Bank" style={{ color: colors.foreground }}>Bank</SelectItem>
                                            <SelectItem value="Business" style={{ color: colors.foreground }}>Business</SelectItem>
                                        </SelectContent>
                                    </Select>
                                </motion.div>
                            )}

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
                                        onMouseEnter={(e) => {
                                            e.target.style.backgroundColor = colors.hover || colors.accent;
                                            e.target.style.borderColor = colors.hover || colors.accent;
                                            e.target.style.transform = 'scale(1.02)';
                                        }}
                                        onMouseLeave={(e) => {
                                            e.target.style.backgroundColor = colors.accent;
                                            e.target.style.borderColor = colors.accent;
                                            e.target.style.transform = 'scale(1)';
                                        }}
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
                                    variant="ghost"
                                    onClick={() => {
                                        setIsSignUp(!isSignUp);
                                        setError({username: "", password: "", rePassword: ""});
                                        setAllow("");
                                        setSuccess("");
                                        setForm({ username: "", password: "", rePassword: "" });
                                        setRole("");
                                    }}
                                    className="text-sm transition-colors duration-300"
                                    style={{
                                        color: colors.accent,
                                        backgroundColor: `${colors.accent}20`,
                                        border: 'none',
                                        textDecoration: 'none'
                                    }}
                                    disabled={isLoading}
                                    onMouseEnter={(e) => {
                                        e.target.style.color = colors.hover || colors.foreground;
                                        e.target.style.transform = 'scale(1.05)';
                                    }}
                                    onMouseLeave={(e) => {
                                        e.target.style.color = colors.accent;
                                        e.target.style.transform = 'scale(1)';
                                    }}
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
                                        <p style={{ color: colors.muted }}>Username: demo_user</p>
                                        <p style={{ color: colors.muted }}>Password: DemoPass123!</p>
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