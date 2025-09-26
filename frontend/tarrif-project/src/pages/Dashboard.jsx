// ====================================
// IMPORTS SECTION
// ====================================

// External libraries
import { useEffect, useState } from 'react' // React hooks for state management and side effects
import { useNavigate } from 'react-router-dom' // Navigation hook
import axios from 'axios' // HTTP client for API requests

// Animation library for smooth transitions
import { motion, AnimatePresence } from 'framer-motion'

// shadcn/ui components - Modern, accessible UI components
import { Button } from '../components/ui/button' // Customizable button component
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card' // Card components
import { Progress } from '../components/ui/progress' // Progress bar component

// Theme and icon components
import { useTheme } from '../contexts/ThemeContext.jsx' // Custom theme context for component-level theming
import { useAuth } from '../contexts/AuthContext.jsx' // Authentication context for user management
import {
    Menu,
    Sun,
    Moon,
    Calculator,
    TrendingUp,
    Globe,
    BarChart3,
    Activity,
    Users,
    DollarSign,
    Clock,
    Star,
    Zap,
    Target,
    Award,
    AlertCircle,
    CheckCircle,
    User,
    Settings,
    LogOut,
    ConstructionIcon
} from 'lucide-react' // SVG icons

// Custom components
import Chart from '../components/Chart.jsx' // Custom chart component
import { Header } from '../components/Header.jsx' // Header component
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

// ====================================
// DASHBOARD COMPONENT
// ====================================

export function Dashboard({ onMenuClick }){
    // ====================================
    // THEME INTEGRATION
    // ====================================

    // Get theme context for component-level color management
    const { colors, theme, toggleTheme, isDark } = useTheme();

    // Get authentication context for user management
    const { user, logout } = useAuth();

    // Toast hook
    const { toast } = useToast();

    // Navigation hook
    const navigate = useNavigate();

    // ====================================
    // STATE VARIABLES
    // ====================================

    // Get backend URL from environment variables (.env file)
    const backendURL = import.meta.env.VITE_BACKEND_URL;

    // Dashboard data
    const [stats, setStats] = useState({
        totalCalculations: 0,
        countriesCovered: 0,
        avgTariffRate: 0,
        recentActivity: []
    });

    // UI state
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    // ====================================
    // EFFECTS
    // ====================================

    // Auto-dismiss error message after 5 seconds
    useEffect(() => {
        if (error) {
            const timer = setTimeout(() => setError(''), 5000);
            return () => clearTimeout(timer);
        }
    }, [error]);

    // Auto-dismiss success message after 5 seconds
    useEffect(() => {
        if (success) {
            const timer = setTimeout(() => setSuccess(''), 5000);
            return () => clearTimeout(timer);
        }
    }, [success]);

    // ====================================
    // DATA FETCHING
    // ====================================

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                setLoading(true);
                // In a real app, you'd fetch actual dashboard data from the backend
                // For now, we'll use mock data
                setTimeout(() => {
                    setStats({
                        totalCalculations: 47,
                        countriesCovered: 89,
                        avgTariffRate: 8.3,
                        recentActivity: [
                            { id: 1, action: "Calculated tariff for USA → China", time: "2 hours ago", type: "calculation" },
                            { id: 2, action: "Viewed historical trends for Singapore → Malaysia", time: "1 day ago", type: "analysis" },
                            { id: 3, action: "Exported tariff report", time: "2 days ago", type: "export" },
                            { id: 4, action: "Updated profile settings", time: "3 days ago", type: "settings" }
                        ]
                    });
                    setLoading(false);

                    // Show market alerts as toasts
                    setTimeout(() => {
                        toast({
                            title: "💡 Optimal Trade Route",
                            description: "Consider Singapore → Vietnam for electronics. Current average tariff: 2.1%",
                            variant: "default",
                            duration: 3000,
                        });
                    }, 1000);

                    setTimeout(() => {
                        toast({
                            title: "⚠️ Market Trend Alert",
                            description: "EU tariffs on Chinese goods increased by 12% in Q4. Monitor closely.",
                            variant: "destructive",
                            duration: 3000,
                        });
                    }, 2000);

                    setTimeout(() => {
                        toast({
                            title: "💰 Cost Saving Opportunity",
                            description: "Switch to Malaysia for manufacturing could save 8.5% on total landed costs.",
                            variant: "success",
                            duration: 3000,
                        });
                    }, 4000);
                }, 1000);
            } catch (error) {
                console.log("Error fetching dashboard data:", error);
                setError("Failed to load dashboard data. Please try again.");
                clearMessages();
                setLoading(false);
            }
        };

        fetchDashboardData();
    }, []);

    const [pinned, setPinned] = useState([]);
    const [showPin, setShowPin] = useState({}); // object: { id: response.data }

    useEffect(() => {
    const storedPins = localStorage.getItem("pin"); // "1,2,3"
        if (storedPins) {
            const pinsArray = storedPins.split(",").map(p => Number(p.trim()));
            setPinned(pinsArray);

            // Fetch for each pinnedId
            pinsArray.forEach(id => pinnedTariffRate(id));
        }
    }, []);

    const pinnedTariffRate = async (pinnedId) => {
        try {
            const response = await axios.post(`${backendURL}/tariff/past/${pinnedId}`);

            // Map pinnedId -> response.data
            setShowPin(prev => ({
            ...prev,
            [pinnedId]: response.data,
            }));

            console.log(pinnedId, response.data);
        } catch (error) {
            console.log(error);
        }
    };

    const seeConsole = () => {
        console.log(showPin[0][showPin[0].length-1]);
    };

    const togglePin = (item) => {
        let updatedPins;
        if (pinned.includes(item)) {
            // Remove the item
            updatedPins = pinned.filter(p => p !== item);
            console.log("removing", item);
            delPin(item); // optional, if you handle backend
        } else {
            // Add the item
            updatedPins = [...pinned, item];
            console.log("adding", item);
            addPin(item); // optional, if you handle backend
        }
        // Update state
        setPinned(updatedPins);
        // Sync to localStorage as string
        localStorage.setItem("pin", updatedPins.join(",")); // "1,2,3"
    };

    const addPin = async(item) => {
        try {
            const response = await axios.post(`${backendURL}/user/${localStorage.getItem("username")}/pinned-tariffs/${item}`);
            localStorage.setItem("pin", response.data);
            console.log(response);
        } catch (error) {
            console.log(error);
        }
    };

    const delPin = async(item) => {
        try {
            const response = await axios.post(`${backendURL}/user/${localStorage.getItem("username")}/unpinned-tariffs/${item}`);
            localStorage.setItem("pin", response.data);
            console.log(response);
        } catch (error) {
            console.log(error);
        }
    };

    // ====================================
    // EVENT HANDLERS
    // ====================================

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    const quickActions = [
        {
            title: "New Calculation",
            description: "Calculate tariffs for new trade routes",
            icon: Calculator,
            action: () => navigate('/calculator'),
            color: colors.accent
        },
        {
            title: "View Trends",
            description: "Analyze historical tariff data",
            icon: TrendingUp,
            action: () => navigate('/calculator'),
            color: "#3b82f6"
        },
        {
            title: "Global Coverage",
            description: "Explore countries and markets",
            icon: Globe,
            action: () => navigate('/'),
            color: "#10b981"
        },
        {
            title: "Settings",
            description: "Manage your account preferences",
            icon: Settings,
            action: () => navigate('/settings'),
            color: "#f59e0b"
        }
    ];

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
            <Header onMenuClick={onMenuClick} showUserInfo={true} />

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

                {error && (
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
                                    {error}
                                </span>
                            </CardContent>
                        </Card>
                    </motion.div>
                )}
            </AnimatePresence>

            {/* MAIN CONTENT */}
            <div className="relative z-10 container mx-auto px-4 py-8">
                <motion.div
                    variants={itemVariants}
                    className="max-w-7xl mx-auto space-y-8"
                >
                    {/* Welcome Section */}
                    <motion.div
                        variants={itemVariants}
                        className="text-center mb-8"
                    >
                        <motion.div
                            initial={{ scale: 0.8, opacity: 0 }}
                            animate={{ scale: 1, opacity: 1 }}
                            transition={{ duration: 0.6, delay: 0.2 }}
                            className="inline-block mb-4"
                        >
                            <div
                                className="p-4 rounded-2xl shadow-2xl mx-auto w-fit"
                                style={{ backgroundColor: colors.accent }}
                            >
                                <User className="h-12 w-12 text-white" />
                            </div>
                        </motion.div>

                        <motion.h1
                            variants={itemVariants}
                            className="text-4xl font-bold mb-4"
                            style={{ color: colors.foreground }}
                        >
                            Welcome back, {user?.username || 'Trader'}! 👋
                        </motion.h1>
                        <motion.p
                            variants={itemVariants}
                            className="text-xl"
                            style={{ color: colors.muted }}
                        >
                            Ready to optimize your global trade strategy?
                        </motion.p>
                    </motion.div>

                    {/* Stats Overview */}
                    <motion.div variants={itemVariants}>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                            {[
                                {
                                    title: "Total Calculations",
                                    value: stats.totalCalculations,
                                    icon: Calculator,
                                    description: "Tariff calculations performed",
                                    color: colors.accent
                                },
                                {
                                    title: "Countries Covered",
                                    value: stats.countriesCovered,
                                    icon: Globe,
                                    description: "Markets analyzed",
                                    color: "#3b82f6"
                                },
                                {
                                    title: "Avg Tariff Rate",
                                    value: `${stats.avgTariffRate}%`,
                                    icon: TrendingUp,
                                    description: "Across all calculations",
                                    color: "#10b981"
                                },
                                {
                                    title: "Premium Member",
                                    value: "Active",
                                    icon: Star,
                                    description: "Since account creation",
                                    color: "#f59e0b"
                                }
                            ].map((stat, index) => (
                                <motion.div
                                    key={stat.title}
                                    initial={{ opacity: 0, y: 20 }}
                                    animate={{ opacity: 1, y: 0 }}
                                    transition={{ delay: 0.1 * index }}
                                >
                                    <Card style={{
                                        backgroundColor: `${colors.surface}95`,
                                        borderColor: colors.border
                                    }}>
                                        <CardContent className="p-6">
                                            <div className="flex items-center justify-between">
                                                <div>
                                                    <p className="text-sm font-medium" style={{ color: colors.muted }}>
                                                        {stat.title}
                                                    </p>
                                                    <p className="text-3xl font-bold" style={{ color: colors.foreground }}>
                                                        {stat.value}
                                                    </p>
                                                    <p className="text-xs" style={{ color: colors.muted }}>
                                                        {stat.description}
                                                    </p>
                                                </div>
                                                <div
                                                    className="p-3 rounded-lg"
                                                    style={{ backgroundColor: stat.color + '20' }}
                                                >
                                                    <stat.icon
                                                        className="h-6 w-6"
                                                        style={{ color: stat.color }}
                                                    />
                                                </div>
                                            </div>
                                        </CardContent>
                                    </Card>
                                </motion.div>
                            ))}
                        </div>
                    </motion.div>


                    {/* Pinned Items */}
                    <Card>
                        <CardTitle>Pinned</CardTitle>
                        <CardContent>
                            {pinned.length > 0 ? (
                                <Card>
                                    {pinned.map(id => {
                                        const rows = showPin[id];       // full array of results
                                        const lastRow = rows ? rows.at(-1) : null; // last row

                                        return (
                                            <div key={id} style={{ marginBottom: "1rem" }}>
                                            <Card>
                                            <h3>Pinned ID: {id}</h3>
                                            {lastRow ? (
                                                <>
                                                <p>
                                                    {lastRow.reportingCountry} → {lastRow.partnerCountry} : {lastRow.tariff}%
                                                </p>
                                                <button onClick={() => togglePin(id)}>Unpin</button>
                                                </>
                                            ) : (
                                                <p>Loading...</p>
                                            )}</Card>
                                            </div>
                                        );
                                    })}
                                </Card>
                            ): null}
                        </CardContent>
                    </Card>
                    

                    {/* Quick Actions */}
                    <motion.div variants={itemVariants}>
                        <Card style={{
                            backgroundColor: `${colors.surface}95`,
                            borderColor: colors.border
                        }}>
                            <CardHeader>
                                <CardTitle style={{ color: colors.foreground }}>
                                    <Zap className="h-6 w-6 inline mr-2" />
                                    Quick Actions
                                </CardTitle>
                                <CardDescription style={{ color: colors.muted }}>
                                    Jump into your most common tasks
                                </CardDescription>
                            </CardHeader>
                            <CardContent>
                                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                                    {quickActions.map((action, index) => (
                                        <motion.div
                                            key={action.title}
                                            whileHover={{ scale: 1.02 }}
                                            whileTap={{ scale: 0.98 }}
                                        >
                                            <Button
                                                onClick={action.action}
                                                className="w-full h-auto p-4 flex flex-col items-center space-y-2"
                                                style={{
                                                    backgroundColor: action.color + '10',
                                                    borderColor: action.color,
                                                    color: action.color
                                                }}
                                                variant="outline"
                                            >
                                                <action.icon className="h-8 w-8" />
                                                <div className="text-center">
                                                    <div className="font-semibold">{action.title}</div>
                                                    <div className="text-xs opacity-75">{action.description}</div>
                                                </div>
                                            </Button>
                                        </motion.div>
                                    ))}
                                </div>
                            </CardContent>
                        </Card>
                    </motion.div>

                    {/* Recent Activity & Insights */}
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                        {/* Recent Activity */}
                        <motion.div variants={itemVariants}>
                            <Card style={{
                                backgroundColor: `${colors.surface}95`,
                                borderColor: colors.border
                            }}>
                                <CardHeader>
                                    <CardTitle style={{ color: colors.foreground }}>
                                        <Clock className="h-6 w-6 inline mr-2" />
                                        Recent Activity
                                    </CardTitle>
                                    <CardDescription style={{ color: colors.muted }}>
                                        Your latest tariff analysis activities
                                    </CardDescription>
                                </CardHeader>
                                <CardContent>
                                    <div className="space-y-4">
                                        {stats.recentActivity.map((activity, index) => (
                                            <motion.div
                                                key={activity.id}
                                                initial={{ opacity: 0, x: -20 }}
                                                animate={{ opacity: 1, x: 0 }}
                                                transition={{ delay: 0.1 * index }}
                                                className="flex items-center space-x-3 p-3 rounded-lg"
                                                style={{ backgroundColor: colors.background }}
                                            >
                                                <div
                                                    className="p-2 rounded-lg"
                                                    style={{
                                                        backgroundColor:
                                                            activity.type === 'calculation' ? colors.accent + '20' :
                                                            activity.type === 'analysis' ? '#3b82f620' :
                                                            activity.type === 'export' ? '#10b98120' : '#f59e0b20'
                                                    }}
                                                >
                                                    {activity.type === 'calculation' && <Calculator className="h-4 w-4" style={{ color: colors.accent }} />}
                                                    {activity.type === 'analysis' && <TrendingUp className="h-4 w-4" style={{ color: '#3b82f6' }} />}
                                                    {activity.type === 'export' && <BarChart3 className="h-4 w-4" style={{ color: '#10b981' }} />}
                                                    {activity.type === 'settings' && <Settings className="h-4 w-4" style={{ color: '#f59e0b' }} />}
                                                </div>
                                                <div className="flex-1">
                                                    <p className="text-sm font-medium" style={{ color: colors.foreground }}>
                                                        {activity.action}
                                                    </p>
                                                    <p className="text-xs" style={{ color: colors.muted }}>
                                                        {activity.time}
                                                    </p>
                                                </div>
                                            </motion.div>
                                        ))}
                                    </div>
                                </CardContent>
                            </Card>
                        </motion.div>

                        {/* Trade Insights */}
                        <motion.div variants={itemVariants}>
                            <Card style={{
                                backgroundColor: `${colors.surface}95`,
                                borderColor: colors.border
                            }}>
                                <CardHeader>
                                    <CardTitle style={{ color: colors.foreground }}>
                                        <Target className="h-6 w-6 inline mr-2" />
                                        Trade Insights
                                    </CardTitle>
                                    <CardDescription style={{ color: colors.muted }}>
                                        AI-powered recommendations for your business
                                    </CardDescription>
                                </CardHeader>
                                <CardContent className="space-y-4">
                                    {/* Alerts moved to toasts */}
                                </CardContent>
                            </Card>
                        </motion.div>
                    </div>

                    {/* Usage Progress */}
                    <motion.div variants={itemVariants}>
                        <Card style={{
                            backgroundColor: `${colors.surface}95`,
                            borderColor: colors.border
                        }}>
                            <CardHeader>
                                <CardTitle style={{ color: colors.foreground }}>
                                    <BarChart3 className="h-6 w-6 inline mr-2" />
                                    Monthly Usage
                                </CardTitle>
                                <CardDescription style={{ color: colors.muted }}>
                                    Your tariff calculation usage this month
                                </CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <div className="space-y-2">
                                    <div className="flex justify-between text-sm">
                                        <span style={{ color: colors.foreground }}>Calculations Used</span>
                                        <span style={{ color: colors.muted }}>47 / 100</span>
                                    </div>
                                    <Progress value={47} className="h-2" />
                                </div>
                                <div className="space-y-2">
                                    <div className="flex justify-between text-sm">
                                        <span style={{ color: colors.foreground }}>Reports Generated</span>
                                        <span style={{ color: colors.muted }}>12 / 50</span>
                                    </div>
                                    <Progress value={24} className="h-2" />
                                </div>
                                <div className="space-y-2">
                                    <div className="flex justify-between text-sm">
                                        <span style={{ color: colors.foreground }}>Data Exports</span>
                                        <span style={{ color: colors.muted }}>3 / 25</span>
                                    </div>
                                    <Progress value={12} className="h-2" />
                                </div>
                            </CardContent>
                        </Card>
                    </motion.div>
                </motion.div>
            </div>
        </motion.div>
    );
}