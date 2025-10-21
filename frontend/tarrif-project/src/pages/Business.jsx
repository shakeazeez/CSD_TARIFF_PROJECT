// ====================================
// IMPORTS SECTION
// ====================================

// External libraries
import { useEffect, useState } from "react"; // React hooks for state management and side effects
import axios from "axios"; // HTTP client for API requests

// Animation library for smooth transitions
import { motion, AnimatePresence } from "framer-motion";

// shadcn/ui components - Modern, accessible UI components
import { Button } from "../components/ui/button"; // Customizable button component
import { Input } from "../components/ui/input"; // Input field component
import { Label } from "../components/ui/label"; // Label component
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "../components/ui/card"; // Card components

// Theme and icon components
import { useTheme } from "../contexts/ThemeContext.jsx"; // Custom theme context for component-level theming
import { useAuth } from "../contexts/AuthContext.jsx"; // Authentication context for user management
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
    CheckCircle,
} from "lucide-react"; // SVG icons

// Custom components
import Dropdown from "../components/Dropdown.jsx"; // Custom dropdown component
import Chart from "../components/Chart.jsx"; // Custom chart component
import { Header } from "../components/Header.jsx"; // Header component
import { useToast } from "../hooks/use-toast";


export function Business({onMenuClick}) {
    // ====================================
    // THEME INTEGRATION
    // ====================================

    // Get theme context for component-level color management
    const { colors, theme, toggleTheme, isDark } = useTheme();
    const backendURL = import.meta.env.VITE_BACKEND_URL;
    // Country data and user selections
    const [list, setList] = useState([]); // Array of all available countries from backend
    const [report, setReport] = useState(""); // Selected reporting country (importer)
    const [partner, setPartner] = useState(""); // Selected partner country (exporter)
    const [tableData, setTableData] = useState([]); // State for the table data

    // Tariff calculation inputs
    const [hs, setHS] = useState(""); // HS Code (Harmonized System code for product classification)
    const [cost, setCost] = useState(); // Item cost in USD

    // useEffect hook: Runs once when component mounts to fetch country data
    useEffect(() => {
        // Async function to fetch all available countries from backend
        const fetchCountry = async () => {
            try {
                // Make GET request to backend countries endpoint
                const response = await axios.get(`${backendURL}/tariff/countries`);

                // Update state with fetched country list
                setList(response.data);
            } catch (error) {
                console.error("Error fetching countries:", error);

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
        const fallbackCountries = [
            { countryName: "United States" },
            { countryName: "China" },
            { countryName: "Singapore" },
            { countryName: "Malaysia" },
            { countryName: "Japan" },
            { countryName: "South Korea" },
            { countryName: "Germany" },
            { countryName: "United Kingdom" },
            { countryName: "France" },
            { countryName: "Canada" }
        ];
        setList(fallbackCountries);
    }, []); // Empty dependency array = run only once on component mount

    const handleAddItem = () => {
        if (report && partner && hs) {
            const newItem = { report, partner, hs };
            setTableData((prevData) => [...prevData, newItem]);
            // Optionally clear inputs after adding
            setHS("");
        }
    };

    const modList =
        list && Array.isArray(list)
            ? list.map((item) => ({
                id: item.countryName, // Display name for dropdown
                code: item.countryName, // Value sent to backend
            }))
            : [];



    return (
        <>
            {/* TOP NAVIGATION */}
            <Header onMenuClick={onMenuClick} showUserInfo={true} />
            {/* Country Selection */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                    <Label
                        htmlFor="reporting-country"
                        style={{ color: colors.foreground }}
                    >
                        Reporting Country (Importer)
                    </Label>
                    <Dropdown
                        options={modList}
                        value={report}
                        onChange={(option) =>
                            setReport(option ? option.code : "")
                        }
                        placeholder="Select reporting country"
                        className="w-full"
                    />
                </div>
                <div className="space-y-2">
                    <Label
                        htmlFor="partner-country"
                        style={{ color: colors.foreground }}
                    >
                        Partner Country (Exporter)
                    </Label>
                    <Dropdown
                        options={modList}
                        value={partner}
                        onChange={(option) =>
                            setPartner(option ? option.code : "")
                        }
                        placeholder="Select partner country"
                        className="w-full"
                    />
                </div>
            </div>

            {/* HS Code and Cost */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                    <Label
                        htmlFor="hs-code"
                        style={{ color: colors.foreground }}
                    >
                        Item/Item Description
                    </Label>
                    <Input
                        id="hs-code"
                        type="text"
                        placeholder="Enter Item/Item Description (Slipper)"
                        value={hs}
                        onChange={(e) => setHS(e.target.value.toLowerCase())}
                        style={{
                            backgroundColor: colors.background,
                            borderColor: colors.border,
                            color: colors.foreground,
                        }}
                    />
                </div>
            </div>
            <Button onClick={handleAddItem}>Add</Button>

            {tableData.length > 0 && (
                <Card className="mt-8">
                    <CardHeader>
                        <CardTitle>Items List</CardTitle>
                        <CardDescription>
                            The items you have added for your business.
                        </CardDescription>
                    </CardHeader>
                    <CardContent>
                        <div className="overflow-x-auto relative">
                            <table className="w-full text-sm text-left">
                                <thead
                                    className="text-xs uppercase"
                                    style={{
                                        backgroundColor: colors.muted,
                                        color: colors.mutedForeground,
                                    }}
                                >
                                    <tr>
                                        <th scope="col" className="px-6 py-3">
                                            Reporting Country (Importer)
                                        </th>
                                        <th scope="col" className="px-6 py-3">
                                            Partner Country (Exporter)
                                        </th>
                                        <th scope="col" className="px-6 py-3">
                                            Item/Item Description
                                        </th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {tableData.map((item, index) => (
                                        <tr
                                            key={index}
                                            className="border-b"
                                            style={{
                                                backgroundColor: colors.card,
                                                borderColor: colors.border,
                                            }}
                                        >
                                            <td className="px-6 py-4">
                                                {item.report}
                                            </td>
                                            <td className="px-6 py-4">
                                                {item.partner}
                                            </td>
                                            <td className="px-6 py-4">
                                                {item.hs}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </CardContent>
                </Card>
            )}
        </>
    );
}