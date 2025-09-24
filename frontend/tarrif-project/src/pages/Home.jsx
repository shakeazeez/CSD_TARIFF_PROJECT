// ====================================
// IMPORTS SECTION
// ====================================

// External libraries
import { useEffect, useState } from 'react' // React hooks for state management and side effects
import { useNavigate } from 'react-router-dom' // Navigation hook

// Animation library for smooth transitions
import { motion, AnimatePresence } from 'framer-motion'

// shadcn/ui components - Modern, accessible UI components
import { Button } from '../components/ui/button' // Customizable button component

// Theme and icon components
import { useTheme } from '../contexts/ThemeContext.jsx' // Custom theme context for component-level theming
import { useAuth } from '../contexts/AuthContext.jsx' // Authentication context for user management
import {
    Calculator,
    Globe,
    TrendingUp,
    ArrowRight,
    Plane,
    Ship,
    MapPin,
    Sparkles,
    Sun,
    Moon,
    Menu
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

// ====================================
// HOMEPAGE COMPONENT
// ====================================

export function Home({ onMenuClick }){
    // ====================================
    // THEME INTEGRATION
    // ====================================

    // Get theme context for component-level color management
    const { colors, toggleTheme, isDark } = useTheme();

    // Get authentication context for user management
    const { user } = useAuth();

    // Navigation hook
    const navigate = useNavigate();

    // ====================================
    // STATE VARIABLES
    // ====================================

    const [animatedElements, setAnimatedElements] = useState([]);

    // ====================================
    // EFFECTS
    // ====================================

    // Generate animated trade route elements
    useEffect(() => {
        const elements = [];
        const countries = [
            { name: 'China', x: 75, y: 45 },
            { name: 'USA', x: 15, y: 35 },
            { name: 'Germany', x: 45, y: 25 },
            { name: 'Japan', x: 85, y: 35 },
            { name: 'India', x: 65, y: 55 },
            { name: 'Brazil', x: 25, y: 65 },
            { name: 'UK', x: 42, y: 22 },
            { name: 'Singapore', x: 72, y: 58 }
        ];

        // Create animated routes between countries
        for (let i = 0; i < countries.length; i++) {
            for (let j = i + 1; j < countries.length; j++) {
                const from = countries[i];
                const to = countries[j];

                // Only create some connections to avoid clutter
                if (Math.random() > 0.7) {
                    elements.push({
                        id: `route-${i}-${j}`,
                        type: Math.random() > 0.5 ? 'plane' : 'ship',
                        from,
                        to,
                        delay: Math.random() * 3,
                        duration: 3 + Math.random() * 2
                    });
                }
            }
        }

        setAnimatedElements(elements);
    }, []);

    // ====================================
    // COMPONENT RENDER (JSX)
    // ====================================

    return(
        <motion.div
            initial="hidden"
            animate="visible"
            variants={containerVariants}
            className="min-h-screen relative overflow-hidden"
            style={{
                background: 'transparent'
            }}
        >



            {/* TOP NAVIGATION */}
            <motion.header
                initial={{ y: -100, opacity: 0 }}
                animate={{ y: 0, opacity: 1 }}
                transition={{ duration: 0.6 }}
                className="backdrop-blur-sm border-b sticky top-0 z-50"
                style={{
                    backgroundColor: `${colors.surface}cc`,
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
                            className="text-white hover:bg-white/10"
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
                            {user ? (
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
                                        onClick={() => navigate('/calculator')}
                                        className="transition-all duration-300 text-white"
                                        style={{
                                            backgroundColor: colors.accent,
                                            borderColor: colors.accent
                                        }}
                                        size="sm"
                                    >
                                        <Calculator className="h-4 w-4 mr-2" />
                                        Calculator
                                    </Button>
                                </div>
                            ) : (
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
                            )}

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
                        </div>
                    </div>
                </div>
            </motion.header>

            {/* HERO SECTION */}
            <div className="relative z-10 flex items-center justify-center min-h-[calc(100vh-80px)]">
                <div className="max-w-4xl mx-auto text-center px-4 sm:px-6 lg:px-8 p-8 rounded-3xl shadow-2xl backdrop-blur-md" style={{
                    backgroundColor: `${colors.surface}90`,
                    border: `1px solid ${colors.border}50`
                }}>
                    <motion.div
                        variants={itemVariants}
                        className="mb-8"
                    >
                        <motion.div
                            animate={{
                                scale: [1, 1.1, 1],
                                rotate: [0, 5, -5, 0]
                            }}
                            transition={{
                                duration: 4,
                                repeat: Infinity,
                                repeatType: "reverse"
                            }}
                            className="inline-block mb-6"
                        >
                            <div
                                className="p-6 rounded-2xl shadow-2xl mx-auto w-fit"
                                style={{ backgroundColor: colors.accent }}
                            >
                                <Globe className="h-16 w-16 text-white" />
                            </div>
                        </motion.div>

                        <motion.h1
                            variants={itemVariants}
                            className="text-4xl sm:text-6xl font-bold mb-6"
                            style={{ color: colors.foreground }}
                        >
                            Global Trade
                            <span className="block" style={{ color: colors.accent }}>
                                Made Simple
                            </span>
                        </motion.h1>

                        <motion.p
                            variants={itemVariants}
                            className="text-xl sm:text-2xl mb-8 max-w-2xl mx-auto"
                            style={{ color: colors.muted }}
                        >
                            Calculate international tariffs, analyze trade costs, and optimize your global business strategy with our professional tariff analysis platform.
                        </motion.p>
                    </motion.div>

                    <motion.div
                        variants={itemVariants}
                        className="flex flex-col sm:flex-row gap-4 justify-center items-center"
                    >
                        <motion.div
                            whileHover={{ scale: 1.05 }}
                            whileTap={{ scale: 0.95 }}
                        >
                            <Button
                                onClick={() => navigate('/calculator')}
                                size="lg"
                                className="text-white px-8 py-4 text-lg font-semibold shadow-xl"
                                style={{
                                    backgroundColor: colors.accent,
                                    borderColor: colors.accent
                                }}
                            >
                                <Calculator className="h-5 w-5 mr-3" />
                                Start Calculating
                                <ArrowRight className="h-5 w-5 ml-3" />
                            </Button>
                        </motion.div>

                        <motion.div
                            whileHover={{ scale: 1.05 }}
                            whileTap={{ scale: 0.95 }}
                        >
                            <Button
                                variant="outline"
                                size="lg"
                                onClick={() => navigate('/faq')}
                                className="px-8 py-4 text-lg font-semibold shadow-xl"
                                style={{
                                    borderColor: colors.accent,
                                    backgroundColor: colors.surface,
                                    color: colors.accent
                                }}
                            >
                                <TrendingUp className="h-5 w-5 mr-3" />
                                Learn More
                            </Button>
                        </motion.div>
                    </motion.div>

                    {/* FEATURE HIGHLIGHTS */}
                    <motion.div
                        variants={itemVariants}
                        className="mt-16 grid grid-cols-1 sm:grid-cols-3 gap-6 max-w-3xl mx-auto"
                    >
                        {[
                            {
                                icon: Globe,
                                title: "Global Coverage",
                                description: "Access tariff data for 200+ countries"
                            },
                            {
                                icon: Calculator,
                                title: "Real-time Calculations",
                                description: "Instant tariff cost analysis"
                            },
                            {
                                icon: TrendingUp,
                                title: "Historical Trends",
                                description: "Track tariff changes over time"
                            }
                        ].map((feature, index) => (
                            <motion.div
                                key={feature.title}
                                initial={{ opacity: 0, y: 20 }}
                                animate={{ opacity: 1, y: 0 }}
                                transition={{ delay: 0.8 + index * 0.1 }}
                                className="p-6 rounded-xl backdrop-blur-sm shadow-lg"
                                style={{
                                    backgroundColor: `${colors.surface}80`,
                                    border: `1px solid ${colors.border}`
                                }}
                            >
                                <feature.icon
                                    className="h-8 w-8 mx-auto mb-3"
                                    style={{ color: colors.accent }}
                                />
                                <h3 className="font-semibold mb-2" style={{ color: colors.foreground }}>
                                    {feature.title}
                                </h3>
                                <p className="text-sm" style={{ color: colors.muted }}>
                                    {feature.description}
                                </p>
                            </motion.div>
                        ))}
                    </motion.div>
                </div>
            </div>

            {/* BOTTOM DECORATION */}
            <div className="absolute bottom-0 left-0 right-0 h-32 bg-gradient-to-t from-white/10 to-transparent pointer-events-none" />
        </motion.div>
    );
}