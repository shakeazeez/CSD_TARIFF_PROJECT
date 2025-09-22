// ====================================
// IMPORTS SECTION
// ====================================

// External libraries
import axios from 'axios' // HTTP client for API calls
import { useEffect, useState } from 'react' // React hooks for state management and side effects

// Custom components
import Dropdown from '../components/Dropdown' // Custom dropdown for country selection
import Chart from '../components/Chart' // Chart component for historical data visualization

// shadcn/ui components - Modern, accessible UI components
import { Button } from '../components/ui/button' // Customizable button component
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card' // Card layout components
import { Input } from '../components/ui/input' // Styled input component
import { Label } from '../components/ui/label' // Form label component

// Theme and icon components
import { ThemeToggle } from '../components/ThemeToggle' // Dark/light mode toggle
import { TrendingUp, Calculator, BarChart3, Globe } from 'lucide-react' // SVG icons

// ====================================
// MAIN HOME COMPONENT
// ====================================

export function Home(){
    // ====================================
    // STATE VARIABLES
    // ====================================
    
    // Get backend URL from environment variables (.env file)
    const backendURL = import.meta.env.VITE_BACKEND_URL;
    
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
    const legend = [past.item];
    
    // Chart title with country pair information
    const title = past.reportingCountry + " Import from " + past.partnerCountry + " (in %)";

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
    // API FUNCTIONS & SIDE EFFECTS
    // ====================================
    
    // useEffect hook: Runs once when component mounts to fetch country data
    useEffect(() => {
        // Async function to fetch all available countries from backend
        const fetchCountry = async() => {
            try{
                // Make GET request to backend countries endpoint
                const response = await axios.get(`${backendURL}/tariff/countries`);
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
    const fetchCurrent = async() =>{
        try{
            // Log the data being sent for debugging
            console.log("Sending DTO:", tariffCalculationQueryDTO);
            
            // POST request to get current tariff calculation
            const response = await axios.post(`${backendURL}/tariff/current`, tariffCalculationQueryDTO);
            console.log("Current tariff calculation success:", response);
            
            // Update state with current tariff results
            setCurrent(response.data);
            console.log("Current tariff data:", current);

        } catch(error){
            console.log("Error fetching current tariff:", error);
        }
    };

    // Function to fetch historical tariff data for chart visualization
    const fetchPast = async() =>{
        try{
            // Log the data being sent for debugging
            console.log("Sending DTO:", tariffCalculationQueryDTO);
            
            // POST request to get historical tariff data
            const response = await axios.post(`${backendURL}/tariff/past`, tariffCalculationQueryDTO);
            console.log("Historical tariff data success:", response);
            
            // Update state with historical tariff data
            setPast(response.data);
            console.log("Historical tariff data:", past);

        } catch(error){
            console.log("Error fetching historical tariff data:", error);
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
        /* Main container with gradient background and dark mode support */
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900">
            
            {/* Tailwind Test Box 
            <div className="fixed top-4 right-4 z-50 bg-red-500 text-white p-4 rounded-lg shadow-lg">
                <p className="text-sm font-bold">Tailwind Test</p>
                <p className="text-xs">If you see this red box, Tailwind is working!</p>
            </div> */}

            {/* HEADER: Sticky navigation bar with logo and theme toggle */}
            <header className="bg-white/80 dark:bg-gray-900/80 backdrop-blur-sm border-b border-gray-200/50 dark:border-gray-700/50 sticky top-0 z-50">
                <div className="w-full px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center py-4">
                        {/* Logo section with icon and site branding */}
                        <div className="flex items-center space-x-3">
                            <div className="p-2 bg-blue-600 rounded-lg">
                                <Globe className="h-6 w-6 text-white" />
                            </div>
                            <div>
                                <h1 className="text-xl font-bold text-gray-900 dark:text-white">TariffCalc</h1>
                                <p className="text-xs text-gray-500 dark:text-gray-400">Global Trade Analysis</p>
                            </div>
                        </div>
                        {/* Dark/Light mode toggle button */}
                        <ThemeToggle />
                    </div>
                </div>
            </header>

            {/* MAIN LAYOUT: Responsive sidebar and content area */}
            <div className="flex flex-col lg:flex-row min-h-[calc(100vh-73px)]">
                {/* LEFT SIDEBAR: Input controls with responsive width */}
                <div className="w-full lg:w-80 xl:w-96 bg-white/60 dark:bg-gray-900/60 backdrop-blur-sm lg:border-r border-b lg:border-b-0 border-gray-200/50 dark:border-gray-700/50 p-4 sm:p-6">
                    <div className="space-y-6">
                        {/* SETTINGS CARD: Main input form for tariff calculations */}
                        <Card className="shadow-lg border-0 bg-white/80 dark:bg-gray-800/80 backdrop-blur-sm">
                            <CardHeader className="pb-3">
                                <CardTitle className="flex items-center space-x-2 text-lg">
                                    <Calculator className="h-5 w-5 text-blue-600" />
                                    <span>Calculation Settings</span>
                                </CardTitle>
                                <CardDescription>
                                    Configure your tariff calculation parameters
                                </CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                {/* REPORTING COUNTRY DROPDOWN: Country from which tariffs are being reported */}
                                <div className="space-y-2">
                                    <Label htmlFor="reporting-country" className="text-sm font-medium flex items-center space-x-1">
                                        <span>Reporting Country</span>
                                        <span className="text-red-500">*</span>
                                    </Label>
                                    <Dropdown 
                                        title="Select Reporting Country"
                                        options={modList}
                                        onChange={e => setReport(e.code)}
                                    />
                                </div>

                                {/* PARTNER COUNTRY DROPDOWN: Trading partner country for tariff calculation */}
                                <div className="space-y-2">
                                    <Label htmlFor="partner-country" className="text-sm font-medium flex items-center space-x-1">
                                        <span>Partner Country</span>
                                        <span className="text-red-500">*</span>
                                    </Label>
                                    <Dropdown 
                                        title="Select Partner Country"
                                        options={modList}
                                        onChange={e => setPartner(e.code)}
                                    />
                                </div>

                                {/* HS CODE INPUT: Harmonized System code for product classification */}
                                <div className="space-y-2">
                                    <Label htmlFor="hs" className="text-sm font-medium flex items-center space-x-1">
                                        <span>HS Code / Item</span>
                                        <span className="text-red-500">*</span>
                                    </Label>
                                    <Input
                                        type="text"
                                        id="hs"
                                        placeholder="e.g., 8703 (Motor cars)"
                                        onChange={e => setHS((e.target.value).toLowerCase())}
                                        className="bg-white/50 dark:bg-gray-800/50"
                                    />
                                </div>

                                {/* COST INPUT: Product value in USD for tariff calculation */}
                                <div className="space-y-2">
                                    <Label htmlFor="cost" className="text-sm font-medium flex items-center space-x-1">
                                        <span>Item Cost (USD)</span>
                                        <span className="text-red-500">*</span>
                                    </Label>
                                    <Input
                                        type="number"
                                        id="cost"
                                        placeholder="Enter cost in USD"
                                        onChange={e => setCost(e.target.value)}
                                        className="bg-white/50 dark:bg-gray-800/50"
                                    />
                                </div>

                                {/* ACTION BUTTONS: Primary calculation controls */}
                                <div className="pt-4 space-y-3">
                                    {/* Current tariff calculation button */}
                                    <Button 
                                        onClick={fetchCurrent}
                                        className="w-full bg-blue-600 hover:bg-blue-700 text-white"
                                        size="lg"
                                    >
                                        <Calculator className="h-4 w-4 mr-2" />
                                        Calculate Current Tariff
                                    </Button>
                                    {/* Historical data retrieval button */}
                                    <Button 
                                        onClick={fetchPast}
                                        variant="outline"
                                        className="w-full"
                                        size="lg"
                                    >
                                        <BarChart3 className="h-4 w-4 mr-2" />
                                        View Historical Data
                                    </Button>
                                </div>
                            </CardContent>
                        </Card>

                        {/* QUICK STATS CARD: Display current form status and summary information */}
                        <Card className="shadow-lg border-0 bg-white/80 dark:bg-gray-800/80 backdrop-blur-sm">
                            <CardHeader className="pb-3">
                                <CardTitle className="text-sm font-medium text-gray-600 dark:text-gray-300">
                                    Quick Info
                                </CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-3">
                                {/* Display total number of available countries */}
                                <div className="flex justify-between items-center text-sm">
                                    <span className="text-gray-500">Available Countries:</span>
                                    <span className="font-semibold text-blue-600">{list.length}</span>
                                </div>
                                {/* Show selected country pair for trade relationship */}
                                <div className="flex justify-between items-center text-sm">
                                    <span className="text-gray-500">Selected Pair:</span>
                                    <span className="font-semibold">
                                        {report && partner ? `${report} → ${partner}` : 'None'}
                                    </span>
                                </div>
                                {/* Display current HS code if entered */}
                                <div className="flex justify-between items-center text-sm">
                                    <span className="text-gray-500">HS Code:</span>
                                    <span className="font-semibold">{hs || 'None'}</span>
                                </div>
                            </CardContent>
                        </Card>
                    </div>
                </div>

                {/* MAIN CONTENT AREA: Results display and hero section */}
                <div className="flex-1 p-4 sm:p-6 lg:p-8 overflow-auto">
                    <div className="w-full space-y-6">
                        {/* HERO SECTION: Welcome banner with app description */}
                        <div className="text-center mb-6 lg:mb-8">
                            {/* Icon container with gradient background */}
                            <div className="flex justify-center mb-4">
                                <div className="p-3 sm:p-4 bg-gradient-to-r from-blue-600 to-indigo-600 rounded-2xl shadow-lg">
                                    <TrendingUp className="h-8 w-8 sm:h-12 sm:w-12 text-white" />
                                </div>
                            </div>
                            {/* Main heading and description */}
                            <h1 className="text-2xl sm:text-3xl lg:text-4xl font-bold text-gray-900 dark:text-white mb-3">
                                Global Tariff Calculator
                            </h1>
                            <p className="text-base sm:text-lg text-gray-600 dark:text-gray-300 px-4 lg:px-0">
                                Analyze import tariffs, compare historical trade data, and make informed business decisions with real-time tariff calculations.
                            </p>
                        </div>

                        {/* CURRENT RESULTS SECTION: Display tariff calculations when available */}
                        {(current.tariffRate || current.tariffAmount || current.itemCostWithTariff) && (
                            <Card className="shadow-xl border-0 bg-white/80 dark:bg-gray-800/80 backdrop-blur-sm">
                                <CardHeader>
                                    <CardTitle className="text-2xl text-green-600 dark:text-green-400 flex items-center space-x-2">
                                        <div className="p-2 bg-green-100 dark:bg-green-900/30 rounded-lg">
                                            <Calculator className="h-6 w-6 text-green-600 dark:text-green-400" />
                                        </div>
                                        <span>Current Tariff Results</span>
                                    </CardTitle>
                                    <CardDescription className="text-base">
                                        Latest tariff calculations for {report} importing from {partner}
                                    </CardDescription>
                                </CardHeader>
                                <CardContent>
                                    {/* Results grid displaying three key metrics */}
                                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 lg:gap-6">
                                        
                                        {/* TARIFF RATE CARD: Shows percentage rate applied */}
                                        <Card className="bg-gradient-to-br from-blue-50 to-blue-100 dark:from-blue-950/20 dark:to-blue-900/20 border-blue-200 dark:border-blue-800 shadow-lg">
                                            <CardContent className="pt-6">
                                                <div className="text-center">
                                                    <div className="p-3 bg-blue-600 rounded-xl inline-block mb-3">
                                                        <TrendingUp className="h-6 w-6 text-white" />
                                                    </div>
                                                    <p className="text-sm font-medium text-blue-600 dark:text-blue-400 mb-2">
                                                        Tariff Rate
                                                    </p>
                                                    <p className="text-2xl sm:text-3xl lg:text-4xl font-bold text-blue-700 dark:text-blue-300">
                                                        {current.tariffRate ? `${current.tariffRate}%` : '—'}
                                                    </p>
                                                </div>
                                            </CardContent>
                                        </Card>

                                        {/* TARIFF AMOUNT CARD: Shows dollar amount of tariff */}
                                        <Card className="bg-gradient-to-br from-green-50 to-green-100 dark:from-green-950/20 dark:to-green-900/20 border-green-200 dark:border-green-800 shadow-lg">
                                            <CardContent className="pt-6">
                                                <div className="text-center">
                                                    <div className="p-3 bg-green-600 rounded-xl inline-block mb-3">
                                                        <Calculator className="h-6 w-6 text-white" />
                                                    </div>
                                                    <p className="text-sm font-medium text-green-600 dark:text-green-400 mb-2">
                                                        Tariff Amount
                                                    </p>
                                                    <p className="text-4xl font-bold text-green-700 dark:text-green-300">
                                                        {current.tariffAmount ? `$${Number(current.tariffAmount).toLocaleString()}` : '—'}
                                                    </p>
                                                </div>
                                            </CardContent>
                                        </Card>

                                        {/* TOTAL COST CARD: Shows final cost including tariff */}
                                        <Card className="bg-gradient-to-br from-purple-50 to-purple-100 dark:from-purple-950/20 dark:to-purple-900/20 border-purple-200 dark:border-purple-800 shadow-lg">
                                            <CardContent className="pt-6">
                                                <div className="text-center">
                                                    <div className="p-3 bg-purple-600 rounded-xl inline-block mb-3">
                                                        <Globe className="h-6 w-6 text-white" />
                                                    </div>
                                                    <p className="text-sm font-medium text-purple-600 dark:text-purple-400 mb-2">
                                                        Total Cost
                                                    </p>
                                                    <p className="text-2xl sm:text-3xl lg:text-4xl font-bold text-purple-700 dark:text-purple-300">
                                                        {current.itemCostWithTariff ? `$${Number(current.itemCostWithTariff).toLocaleString()}` : '—'}
                                                    </p>
                                                </div>
                                            </CardContent>
                                        </Card>
                                    </div>
                                </CardContent>
                            </Card>
                        )}

                        {/* HISTORICAL CHART SECTION: Display historical tariff trends when data is available */}
                        {past.tariffData && past.tariffData.length > 0 && (
                            <Card className="shadow-xl border-0 bg-white/80 dark:bg-gray-800/80 backdrop-blur-sm">
                                <CardHeader>
                                    <CardTitle className="text-xl lg:text-2xl flex items-center space-x-2">
                                        <div className="p-2 bg-indigo-100 dark:bg-indigo-900/30 rounded-lg">
                                            <BarChart3 className="h-5 w-5 lg:h-6 lg:w-6 text-indigo-600 dark:text-indigo-400" />
                                        </div>
                                        <span>Historical Tariff Analysis</span>
                                    </CardTitle>
                                    <CardDescription className="text-sm lg:text-base">
                                        {title}
                                    </CardDescription>
                                </CardHeader>
                                <CardContent>
                                    {/* Chart container with subtle background gradient */}
                                    <div className="bg-gradient-to-br from-gray-50 to-white dark:from-gray-800 dark:to-gray-900 rounded-xl p-4 lg:p-6 shadow-inner overflow-x-auto">
                                        <Chart labels={labels} value={value} title={title} legend={legend}/>
                                    </div>
                                </CardContent>
                            </Card>
                        )}

                        {/* EMPTY STATE: Welcome screen when no data is loaded */}
                        {!current.tariffRate && !current.tariffAmount && !current.itemCostWithTariff && (!past.tariffData || past.tariffData.length === 0) && (
                            <Card className="text-center py-8 lg:py-16 shadow-xl border-0 bg-white/80 dark:bg-gray-800/80 backdrop-blur-sm">
                                <CardContent>
                                    {/* Large decorative icon with gradient background */}
                                    <div className="p-4 lg:p-6 bg-gradient-to-br from-blue-100 to-indigo-100 dark:from-blue-900/20 dark:to-indigo-900/20 rounded-2xl inline-block mb-6">
                                        <TrendingUp className="h-12 w-12 lg:h-16 lg:w-16 text-blue-600 dark:text-blue-400 mx-auto" />
                                    </div>
                                    {/* Welcome message and instructions */}
                                    <CardTitle className="text-xl lg:text-2xl mb-4 text-gray-900 dark:text-white">Ready to Calculate Tariffs</CardTitle>
                                    <CardDescription className="text-base lg:text-lg px-4 lg:px-0 lg:max-w-md mx-auto text-gray-600 dark:text-gray-300">
                                        Configure your calculation settings in the sidebar and click "Calculate Current Tariff" to get started with your comprehensive tariff analysis.
                                    </CardDescription>
                                </CardContent>
                            </Card>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}