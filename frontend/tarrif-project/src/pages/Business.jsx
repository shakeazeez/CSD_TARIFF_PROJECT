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
import { useAuth } from "../contexts/use-auth.js"; // Authentication context for user management
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
    LetterTextIcon,
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

    // auth / username resolution
    const { user } = useAuth?.() ?? {}; // gracefully handle if useAuth not available
    const username = user?.username || localStorage.getItem("username") || "demo";

    // Country data and user selections
    const [report, setReport] = useState(""); // Selected reporting country (importer)
    const [partner, setPartner] = useState(""); // Selected partner country (exporter)
    const [tableData, setTableData] = useState([]); // State for the table data (unused but kept)
    const [list, setList] = useState([]); // List of countries from backend
    // Tariff calculation inputs
    const [hs, setHS] = useState(""); // HS Code / item description
    const [cost, setCost] = useState(); // Item cost in USD - not currently used

    // local items state (normalized to same shape used in UI)
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
    const [items, setItems] = useState(presetListJSON);

    // Helper: normalize backend response into items shape used by UI
    const normalizeToItems = (data) => {
        if (!data) return presetListJSON;
        // If already an array of items in the expected shape
        if (Array.isArray(data) && data.length && data[0].report) return data;
        // If response is an object that contains items list
        if (Array.isArray(data.items)) return data.items;
        if (Array.isArray(data.businessItems)) return data.businessItems;
        // If response has a map of report -> partners, convert (best-effort)
        if (typeof data === "object") {
            const arr = [];
            for (const key of Object.keys(data)) {
                if (Array.isArray(data[key].partner)) {
                    arr.push({
                        report: key,
                        partner: data[key].partner,
                        hsCode: data[key].hsCode || data[key].hs || [],
                        rate: data[key].rate || []
                    });
                }
            }
            if (arr.length) return arr;
        }
        return presetListJSON;
    };

    // Fetch business items for the current user from backend: GET /business/{username}
    const fetchBusinessItems = useCallback(async () => {
        if (!username) return;
        try {
            const resp = await axios.get(`${backendURL}/business/${encodeURIComponent(username)}`);
            const data = resp?.data;
            const normalized = normalizeToItems(data);
            setItems(normalized);
            setTableData(normalized); // keep tableData in sync if used elsewhere
        } catch (error) {
            console.error("Error fetching business items:", error);
            // fallback: keep existing items (or use preset)
            // setItems(presetListJSON);
        }
    }, [backendURL, username]);

    useEffect(() => {
        fetchBusinessItems();
    }, [fetchBusinessItems]);

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
      }
    };

    // Execute the fetch function
    fetchCountry();
  }, [backendURL]); // run on mount / backendURL change


    const modList =
        list && Array.isArray(list)
            ? list.map((item) => ({
                id: item.countryName, // Display name for dropdown
                code: item.countryName, // Value sent to backend
            }))
            : [];

    // Add item: POST to /business/{username}/items
    const handleAddItem = async () => {
        if (!report || !partner || !hs) return;

        // fetch current rate from tariff service first (best-effort)
        let currentRate = 5; // default fallback
        try {
            const response = await axios.post(`${backendURL}/tariff/current`, {
                reportingCountry: report,
                partnerCountry: partner,
                item: hs,
                itemCost: 1000 // dummy cost, magic number
            });
            const fetched = response?.data;
            const rateFromResp = fetched?.tariffRate ?? fetched?.rate ?? fetched?.value ?? null;
            if (typeof rateFromResp === "number" && !Number.isNaN(rateFromResp)) {
                currentRate = rateFromResp;
            } else if (typeof rateFromResp === "string" && !Number.isNaN(Number(rateFromResp))) {
                currentRate = Number(rateFromResp);
            }
        } catch (err) {
            console.warn("Could not fetch tariff rate, using fallback rate:", err);
        }

        // Build payload expected by backend ReceiveListDTO: { information: [ ... ] }
        const payload = {
            information: [
                {
                    report,
                    partner,
                    hsCode: hs,
                    rate: currentRate
                }
            ]
        };

        try {
            await axios.post(`${backendURL}/business/${encodeURIComponent(username)}/items`, payload);
            // refresh from server after successful add
            await fetchBusinessItems();
        } catch (err) {
            console.error("Error adding item to backend, falling back to local update:", err);
            // fallback: update local items state
            const newItems = items.map((it) => ({
                ...it,
                partner: Array.isArray(it.partner) ? [...it.partner] : [],
                hsCode: Array.isArray(it.hsCode) ? [...it.hsCode] : [],
                rate: Array.isArray(it.rate) ? [...it.rate] : [],
            }));
            let entry = newItems.find(item => item.report === report);
            if (entry) {
                entry.partner.push(partner);
                entry.hsCode.push(hs);
                entry.rate.push(currentRate);
            } else {
                newItems.push({
                    report,
                    partner: [partner],
                    hsCode: [hs],
                    rate: [currentRate]
                });
            }
            setItems(newItems);
        }

        // Clear inputs
        setHS("");
        setPartner("");
    };

    // Delete an item: call DELETE /business/{username}/items with body { information: [...] }
    const handleDelete = async (reportingCountry, partnerIndex) => {
        // find the item details from current state
        const entry = items.find(it => it.report === reportingCountry);
        if (!entry) return;
        const partnerName = entry.partner?.[partnerIndex];
        const hsCode = entry.hsCode?.[partnerIndex];

        if (!partnerName) return;

        const payload = {
            information: [
                {
                    report: reportingCountry,
                    partner: partnerName,
                    hsCode: hsCode
                }
            ]
        };

        try {
            // axios.delete with body requires { data: payload } as second arg
            await axios.delete(`${backendURL}/business/${encodeURIComponent(username)}/items`, { data: payload });
            // refresh from server
            await fetchBusinessItems();
        } catch (err) {
            console.error("Error deleting item on backend, falling back to local delete:", err);
            // fallback: local state update
            const newItems = items.map(item => {
                if (item.report === reportingCountry) {
                    return {
                        ...item,
                        partner: item.partner.filter((_, idx) => idx !== partnerIndex),
                        hsCode: item.hsCode.filter((_, idx) => idx !== partnerIndex),
                        rate: item.rate.filter((_, idx) => idx !== partnerIndex)
                    };
                }
                return item;
            }).filter(item => item.partner.length > 0); // Remove empty entries

            setItems(newItems);
        }
    };

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

            {items.length > 0 && (
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
                                        <th scope="col" className="px-6 py-3">
                                            Actions
                                        </th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {items.map((entry, idx) => (
                                        entry.partner.map((partnerCountry, pIdx) => (
                                            <tr key={`${idx}-${pIdx}`}>
                                                <td className="px-4 py-2">{entry.report}</td>
                                                <td className="px-4 py-2">{partnerCountry}</td>
                                                <td className="px-4 py-2">{entry.hsCode[pIdx]}</td>
                                                <td className="px-4 py-2">{entry.rate[pIdx]}%</td>
                                                <td className="px-4 py-2">
                                                    <Button
                                                        variant="destructive"
                                                        size="sm"
                                                        onClick={() => handleDelete(entry.report, pIdx)}>
                                                        Delete
                                                    </Button>
                                                </td>
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