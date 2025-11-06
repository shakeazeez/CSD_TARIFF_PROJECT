// ====================================
// IMPORTS SECTION
// ====================================

// External libraries
import { useEffect, useState, useCallback, useMemo } from "react"; // React hooks for state management and side effects
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
import { useTheme } from "../contexts/use-theme.js"; // Custom theme context for component-level theming
// import { useAuth } from "../contexts/AuthContext.jsx"; // Authentication context for user management
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
// import { useToast } from "../hooks/use-toast";


export function Business({onMenuClick}) {
    // ====================================
    // THEME INTEGRATION
    // ====================================

    // Get theme context for component-level color management
    const { colors } = useTheme();
    const backendURL = import.meta.env.VITE_BACKEND_URL;
    // Country data and user selections
    // const [report, setReport] = useState(""); // Selected reporting country (importer) - form commented out
    // const [partner, setPartner] = useState(""); // Selected partner country (exporter) - form commented out
    const [tableData, setTableData] = useState([]); // State for the table data

    // Tariff calculation inputs
    // const [hs, setHS] = useState(""); // HS Code - form commented out
    // const [cost, setCost] = useState(); // Item cost in USD - not currently used

    // preset list of stuff for testing
    /**
     * presetList consists of objects with the following structure:
     * String report
     * List<String> partner
     * List<String> hsCode
     * List<Integer> rate
     */
    // Preset list of business items for testing
    const presetList = useMemo(() => [
        { report: "United States", partner: "Mexico", hsCode: "slipper", rate: 5 },
        { report: "United States", partner: "Germany", hsCode: "slipper", rate: 7 },
        { report: "Canada", partner: "Mexico", hsCode: "slipper", rate: 6 },
    ], []);

    // json of how it maps
    // report: String
    // partnet: {String, String, String}
    // hsCode: {String, String, String}
    // rate: {Integer, Integer, Integer}
    const presetListJSON = [
        {
            report: "United States",
            partner: ["Germany", "Canada", "Mexico"],
            hsCode: ["slipper", "boot", "sandal"],
            rate: [7, 5, 6]
        },
        {
            report: "Canada",
            partner: ["Mexico", "United States", "Germany"],
            hsCode: ["slipper", "boot", "sandal"],
            rate: [6, 4, 8]
        },
    ];


    // modList commented out as form is commented out
    // login -> certain values not null -> itemList returns business page 
    // Put in get mapping 
    // Get mapping is business/{username}

    const fetchBusinessItems = useCallback(async () => {
        try {
            const response = await axios.get(
                `${backendURL}/business/items`
            );
            setTableData(response.data);
        } catch (error) {
            console.error("Error fetching business items:", error);
            setTableData(presetList); // Fallback to preset list on error
        }
    }, [backendURL, presetList]);

    useEffect(() => {
        fetchBusinessItems();
    }, [fetchBusinessItems]);


    return (
        <>
            {/* TOP NAVIGATION */}
            <Header onMenuClick={onMenuClick} showUserInfo={true} />
            {/* Country Selection */}
            {/* <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
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
            </div> */}

            {/* HS Code and Cost */}
            {/* <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
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
            <Button onClick={handleAddItem}>Add</Button> */}

            {tableData.length > 0 && (
                <Card className="mt-8">
                    <CardHeader>
                        <CardTitle>Items List</CardTitle>
                        <CardDescription>
                            List of items with tariff rates based on selected countries.
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
                                        <th scope="col" className="px-6 py-3">
                                            Tariff Rate
                                        </th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {tableData.map((item, index) => (
                                        <tr
                                            key={index}
                                            className="border-b"
                                        >
                                            <td className="px-6 py-4">
                                                {item.report}
                                            </td>
                                            <td className="px-6 py-4">
                                                {item.partner}
                                            </td>
                                            <td className="px-6 py-4">
                                                {item.hsCode}
                                            </td>
                                            <td className="px-6 py-4">
                                                {item.rate}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </CardContent>
                    {/* presetjson */}
                    <CardContent> 
                        <div className="text-sm text-gray-500">
                            Note: Tariff rates are based on preset data for demonstration purposes.
                            <table>
                                <thead>
                                    <tr>
                                        <th className="px-4 py-2">Reporting Country</th>
                                        <th className="px-4 py-2">Partner Countries</th>
                                        <th className="px-4 py-2">HS Codes</th>
                                        <th className="px-4 py-2">Rates (%)</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {presetListJSON.map((entry, idx) => (
                                        entry.partner.map((partnerCountry, pIdx) => (
                                            <tr key={`${idx}-${pIdx}`}>
                                                <td className="px-4 py-2">{entry.report}</td>
                                                <td className="px-4 py-2">{partnerCountry}</td>
                                                <td className="px-4 py-2">{entry.hsCode[pIdx]}</td>
                                                <td className="px-4 py-2">{entry.rate[pIdx]}%</td>
                                            </tr>
                                        ))
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