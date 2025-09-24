// ====================================
// IMPORTS SECTION
// ====================================

// External libraries
import axios from 'axios' // HTTP client for API calls
import { useEffect, useState } from 'react' // React hooks for state management and side effects

// Theme and icon components
import { useTheme } from '../contexts/ThemeContext.jsx' // Custom theme context for component-level theming

// ====================================
// MAIN HOME COMPONENT
// ====================================

export function Home(){
    // ====================================
    // THEME INTEGRATION
    // ====================================
    
    // Get theme context for component-level color management
    const { colors, theme, toggleTheme, isDark } = useTheme();
    
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

}