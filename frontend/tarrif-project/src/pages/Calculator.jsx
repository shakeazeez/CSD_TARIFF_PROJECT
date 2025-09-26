// ====================================
// IMPORTS SECTION
// ====================================

// External libraries
import { useEffect, useState } from 'react' // React hooks for state management and side effects
import axios from 'axios' // HTTP client for API requests

// Animation library for smooth transitions
import { motion, AnimatePresence } from 'framer-motion'

// shadcn/ui components - Modern, accessible UI components
import { Button } from '../components/ui/button' // Customizable button component
import { Input } from '../components/ui/input' // Input field component
import { Label } from '../components/ui/label' // Label component
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card' // Card components

// Theme and icon components
import { useTheme } from '../contexts/ThemeContext.jsx' // Custom theme context for component-level theming
import { useAuth } from '../contexts/AuthContext.jsx' // Authentication context for user management
import {
    Calculator as CalculatorIcon,
    Menu,
    Sun,
    Moon,
    TrendingUp,
    Globe,
    ArrowRight,
    RefreshCw,
    AlertCircle,
    CheckCircle
} from 'lucide-react' // SVG icons

// Custom components
import Dropdown from '../components/Dropdown.jsx' // Custom dropdown component
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
// CALCULATOR COMPONENT
// ====================================

export function Calculator({ onMenuClick }){
    // ====================================
    // THEME INTEGRATION
    // ====================================

    // Get theme context for component-level color management
    const { colors, theme, toggleTheme, isDark } = useTheme();

    // Toast hook
    // ====================================
    // STATE VARIABLES
    // ====================================

    // Get backend URLs from environment variables (.env file)
    const tariffURL = import.meta.env.VITE_TARIFF_API_URL;
    const userURL = import.meta.env.VITE_USER_API_URL;

    // Country data and user selections
    const [list, setList] = useState([]); // Array of all available countries from backend
    const [report, setReport] = useState(""); // Selected reporting country (importer)
    const [partner, setPartner] = useState(""); // Selected partner country (exporter)

    // Tariff calculation inputs
    const [hs, setHS] = useState(""); // HS Code (Harmonized System code for product classification)
    const [cost, setCost] = useState(); // Item cost in USD

    // API response data
    const [current, setCurrent] = useState([]); // Current tariff calculation results
    const [past, setPast] = useState([]); // Historical tariff data for charts

    // Loading states
    const [loadingCurrent, setLoadingCurrent] = useState(false);
    const [loadingPast, setLoadingPast] = useState(false);

    // Error and success messages
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    // Pinning
    const [pinned, setPinned] = useState([]);

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
    // DATA PROCESSING & TRANSFORMATION
    // ====================================

    // Transform country list for dropdown component compatibility
    // Converts backend format to {id, code} format expected by Dropdown component
    const modList = list.map(item => ({
        id: item.countryName,   // Display name for dropdown
        code: item.countryName  // Value sent to backend
    }));

    // Chart data preparation from historical tariff data
    // Extract tariff rates for chart visualization
    const value = [(past.tariffData || []).map(item => item.tariffRate)];

    // Extract time periods for chart X-axis labels
    const labels = (past.tariffData || []).map(item => item.startPeriod);

    // Chart legend (typically the item/product name)
    const legend = [past.item || "Tariff Rate"];

    // Chart title with country pair information
    const title = past.reportingCountry && past.partnerCountry
        ? `${past.reportingCountry} Import from ${past.partnerCountry} (in %)`
        : "Historical Tariff Trends";

    // ====================================
    // API REQUEST DATA TRANSFER OBJECTS (DTOs)
    // ====================================

    // DTO for current tariff calculation API call
    const tariffCalculationQueryDTO = {
        reportingCountry: report, // Country doing the importing
        partnerCountry: partner,  // Country exporting the goods
        item: hs,                 // HS Code for product classification
        itemCost: cost           // Cost of the item in USD
    }

    // DTO for historical tariff data (overview) API call
    const tariffOverviewQueryDTO = {
        reportingCountry: report, // Country doing the importing
        partnerCountry: partner,  // Country exporting the goods
        item: hs                 // HS Code for product classification
    }

    // ====================================
    // UTILITY FUNCTIONS
    // ====================================

    // useEffect hook: Runs once when component mounts to fetch country data
    useEffect(() => {
        // Async function to fetch all available countries from backend
        const fetchCountry = async() => {
            try{
                // Make GET request to backend countries endpoint
                const response = await axios.get(`${tariffURL}/tariff/countries`);
                console.log("Fetched countries:", response.data);

                // Update state with fetched country list
                setList(response.data);

            } catch(error){
                console.log("Error fetching countries:", error);

                // FALLBACK DATA: Uncomment below for development/testing without backend
                // const fallbackCountries = [
                //     { countryName: "United States" },
                //     { countryName: "China" },
                //     { countryName: "Singapore" },
                //     { countryName: "Malaysia" },
                //     { countryName: "Japan" },
                //     { countryName: "South Korea" },
                //     { countryName: "Germany" },
                //     { countryName: "United Kingdom" },
                //     { countryName: "France" },
                //     { countryName: "Canada" }
                // ];
                // setList(fallbackCountries);
            }
        };

        // Execute the fetch function
        fetchCountry();
    },[]); // Empty dependency array = run only once on component mount

    // ====================================
    // USER ACTION HANDLERS
    // ====================================

    // Function to fetch current tariff calculation from backend
    const fetchCurrent = async() => {
        if (!report || !partner || !hs || !cost) {
            setError("Please fill in all fields before calculating.");
            return;
        }

        setLoadingCurrent(true);
        setError("");
        setSuccess("");

        try{
            // Log the data being sent for debugging
            console.log("Sending DTO:", tariffCalculationQueryDTO);

            // POST request to get current tariff calculation
            const response = await axios.post(`${tariffURL}/tariff/current`, tariffCalculationQueryDTO);
            console.log("Current tariff calculation success:", response);

            // Update state with current tariff results
            setCurrent(response.data);
            console.log("Current tariff data:", current);

            setSuccess("Tariff calculation completed successfully!");

        } catch(error){
            console.log("Error fetching current tariff:", error);
            setError(error.response?.data?.message || "Error calculating tariff. Please check your inputs and try again.");
        } finally {
            setLoadingCurrent(false);
        }
    };

    // Function to fetch historical tariff data for chart visualization
    const fetchPast = async() =>{
        if (!report || !partner || !hs) {
            setError("Please fill in reporting country, partner country, and HS code before viewing historical data.");
            return;
        }

        setLoadingPast(true);
        setError("");
        setSuccess("");

        try{
            // Log the data being sent for debugging
            console.log("Sending DTO:", tariffCalculationQueryDTO);

            // POST request to get historical tariff data
            const response = await axios.post(`${tariffURL}/tariff/past`, tariffCalculationQueryDTO);
            console.log("Historical tariff data success:", response);

            // Update state with historical tariff data
            setPast(response.data);
            console.log("Historical tariff data:", past);

            setSuccess("Historical data loaded successfully!");

        } catch(error){
            console.log("Error fetching historical tariff data:", error);
            setError(error.response?.data?.message || "Error fetching historical data. Please check your inputs and try again.");
        } finally {
            setLoadingPast(false);
        }
    };

    // Function to add to pin
    const togglePin = (item) => {
        if (pinned.find(p => p.id === item.id)) {
            setPinned(pinned.filter(p => p.id !== item.id));
            console.log("removing " + item);
            delPin(item);
        } else {
            setPinned([...pinned, item]);
            console.log("adding " + item);
            addPin(item);
        }
    };

    const addPin = async(item) => {
        try {
            const response = await axios.post(`${userURL}/user/${localStorage.getItem("username")}/pinned-tariffs/${item}`);
            localStorage.setItem("pin", response.data);
            console.log(response);
        } catch (error) {
            console.log(error);
        }
    };

    const delPin = async(item) => {
        try {
            const response = await axios.post(`${userURL}/user/${localStorage.getItem("username")}/unpinned-tariffs/${item}`);
            localStorage.setItem("pin", response.data);
            console.log(response);
        } catch (error) {
            console.log(error);
        }
    };

    // Debug function to print current form values to console
    const testPrint = async() => {
        console.log("HS Code:", hs);
        console.log("Partner Country:", partner);
        console.log("Reporting Country:", report);
    };

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
                    className="max-w-6xl mx-auto space-y-8"
                >
                    {/* Page Header */}
                    <motion.div
                        variants={itemVariants}
                        className="text-center mb-8"
                    >
                        <h1 className="text-4xl font-bold mb-4" style={{ color: colors.foreground }}>
                            Tariff Calculator
                        </h1>
                        <p className="text-xl" style={{ color: colors.muted }}>
                            Calculate international tariffs and analyze historical trends
                        </p>
                    </motion.div>

                    {/* Calculator Form */}
                    <motion.div variants={itemVariants}>
                        <Card style={{
                            backgroundColor: `${colors.surface}95`,
                            borderColor: colors.border
                        }}>
                            <CardHeader>
                                <CardTitle style={{ color: colors.foreground }}>
                                    <CalculatorIcon className="h-6 w-6 inline mr-2" />
                                    Tariff Calculation
                                </CardTitle>
                                <CardDescription style={{ color: colors.muted }}>
                                    Enter your trade details to calculate current tariffs
                                </CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-6">
                                {/* Country Selection */}
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                    <div className="space-y-2">
                                        <Label htmlFor="reporting-country" style={{ color: colors.foreground }}>
                                            Reporting Country (Importer)
                                        </Label>
                                        <Dropdown
                                            options={modList}
                                            value={report}
                                            onChange={(option) => setReport(option ? option.code : "")}
                                            placeholder="Select reporting country"
                                            className="w-full"
                                        />
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="partner-country" style={{ color: colors.foreground }}>
                                            Partner Country (Exporter)
                                        </Label>
                                        <Dropdown
                                            options={modList}
                                            value={partner}
                                            onChange={(option) => setPartner(option ? option.code : "")}
                                            placeholder="Select partner country"
                                            className="w-full"
                                        />
                                    </div>
                                </div>

                                {/* HS Code and Cost */}
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                    <div className="space-y-2">
                                        <Label htmlFor="hs-code" style={{ color: colors.foreground }}>
                                            HS Code
                                        </Label>
                                        <Input
                                            id="hs-code"
                                            type="text"
                                            placeholder="Enter HS code (e.g., 123456)"
                                            value={hs}
                                            onChange={(e) => setHS(e.target.value)}
                                            style={{
                                                backgroundColor: colors.background,
                                                borderColor: colors.border,
                                                color: colors.foreground
                                            }}
                                        />
                                    </div>
                                    <div className="space-y-2">
                                        <Label htmlFor="cost" style={{ color: colors.foreground }}>
                                            Item Cost (USD)
                                        </Label>
                                        <Input
                                            id="cost"
                                            type="number"
                                            placeholder="Enter item cost"
                                            value={cost}
                                            onChange={(e) => setCost(e.target.value)}
                                            style={{
                                                backgroundColor: colors.background,
                                                borderColor: colors.border,
                                                color: colors.foreground
                                            }}
                                        />
                                    </div>
                                </div>

                                {/* Action Buttons */}
                                <div className="flex flex-col sm:flex-row gap-4">
                                    <Button
                                        onClick={fetchCurrent}
                                        disabled={loadingCurrent}
                                        className="flex-1"
                                        style={{
                                            backgroundColor: colors.accent,
                                            borderColor: colors.accent
                                        }}
                                        onMouseEnter={(e) => {
                                            e.target.style.backgroundColor = colors.hover;
                                            e.target.style.color = '#ffffff';
                                        }}
                                        onMouseLeave={(e) => {
                                            e.target.style.backgroundColor =  `${colors.accent}20`;
                                            e.target.style.color = colors.accent;
                                        }}
                                    >
                                        {loadingCurrent ? (
                                            <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                                        ) : (
                                            <CalculatorIcon className="h-4 w-4 mr-2" />
                                        )}
                                        Calculate Current Tariff
                                    </Button>
                                    <Button
                                        onClick={fetchPast}
                                        disabled={loadingPast}
                                        variant="outline"
                                        className="flex-1"
                                        style={{
                                            borderColor: colors.accent,
                                            color: colors.accent
                                        }}
                                        onMouseEnter={(e) => {
                                            e.target.style.backgroundColor = colors.hover;
                                            e.target.style.color = '#ffffff';
                                        }}
                                        onMouseLeave={(e) => {
                                            e.target.style.backgroundColor = `${colors.accent}20`;
                                            e.target.style.color = colors.accent;
                                        }}
                                    >
                                        {loadingPast ? (
                                            <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                                        ) : (
                                            <TrendingUp className="h-4 w-4 mr-2" />
                                        )}
                                        View Historical Data
                                    </Button>
                                </div>
                            </CardContent>
                        </Card>
                    </motion.div>

                    {/* Current Tariff Results */}
                    {current && Object.keys(current).length > 0 && (
                        <motion.div variants={itemVariants}>
                            <Card style={{
                                backgroundColor: `${colors.surface}95`,
                                borderColor: colors.border
                            }}>
                                <CardHeader>
                                    <CardTitle style={{ color: colors.foreground }}>
                                        <Globe className="h-6 w-6 inline mr-2" />
                                        Current Tariff Results
                                    </CardTitle>
                                    {/* add to pin button */}
                                    <Button className="w-5" onClick={() => togglePin(current.tariffId)}>
                                        {pinned.find(p => p.id === (current.tariffId).id) ? "Unpin" : "Pin"}
                                    </Button>
                                </CardHeader>
                                <CardContent>
                                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                                        {Object.entries(current).map(([key, value]) => (
                                            <div key={key} className="p-4 rounded-lg" style={{
                                                backgroundColor: colors.background,
                                                border: `1px solid ${colors.border}`
                                            }}>
                                                <div className="text-sm font-medium" style={{ color: colors.muted }}>
                                                    {key.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase())}
                                                </div>
                                                <div className="text-2xl font-bold" style={{ color: colors.foreground }}>
                                                    {typeof value === 'number' ? value.toFixed(2) : value}
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </CardContent>
                            </Card>
                        </motion.div>
                    )}

                    {/* Historical Data Chart */}
                    {past && past.tariffData && past.tariffData.length > 0 && (
                        <motion.div variants={itemVariants}>
                            <Card style={{
                                backgroundColor: `${colors.surface}95`,
                                borderColor: colors.border
                            }}>
                                <CardHeader>
                                    <CardTitle style={{ color: colors.foreground }}>
                                        <TrendingUp className="h-6 w-6 inline mr-2" />
                                        Historical Tariff Trends
                                    </CardTitle>
                                    <CardDescription style={{ color: colors.muted }}>
                                        Line chart showing tariff rate changes over time
                                    </CardDescription>
                                </CardHeader>
                                <CardContent>
                                    <Chart
                                        labels={labels}
                                        value={value}
                                        title={title}
                                        legend={legend}
                                    />
                                </CardContent>
                            </Card>
                        </motion.div>
                    )}

                    {/* No Data Message */}
                    {(!current || Object.keys(current).length === 0) &&
                     (!past || !past.tariffData || past.tariffData.length === 0) && (
                        <motion.div variants={itemVariants}>
                            <Card style={{
                                backgroundColor: `${colors.surface}95`,
                                borderColor: colors.border
                            }}>
                                <CardContent className="text-center py-12">
                                    <Globe className="h-16 w-16 mx-auto mb-4" style={{ color: colors.muted }} />
                                    <h3 className="text-xl font-semibold mb-2" style={{ color: colors.foreground }}>
                                        Ready to Calculate
                                    </h3>
                                    <p style={{ color: colors.muted }}>
                                        Fill in the form above and click "Calculate Current Tariff" to get started
                                    </p>
                                </CardContent>
                            </Card>
                        </motion.div>
                    )}
                </motion.div>
            </div>
        </motion.div>
    );
}
