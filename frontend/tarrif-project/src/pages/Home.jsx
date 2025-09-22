import axios from 'axios'
import Dropdown from '../components/Dropdown'
import Chart from '../components/Chart'
import { useEffect, useState } from 'react'


export function Home(){
    const backendURL = import.meta.env.VITE_BACKEND_URL;
    const [list, setList] = useState([]);
    const [report, setReport] = useState("");
    const [partner, setPartner] = useState("");
    const [hs, setHS] = useState("");
    const [cost, setCost] = useState();
    const [current, setCurrent] = useState([]);
    const [past, setPast] = useState([]);
    
    const modList = list.map(item => ({
        id: item.countryName,
        code: item.countryName
    }));

    const value = [(past.tariffData || []).map(item => item.tariffRate)];
    const labels = (past.tariffData || []).map(item => item.startPeriod);
    const legend = [past.item];
    const title = past.reportingCountry + " Import from " + past.partnerCountry + " (in %)";

    const tariffCalculationQueryDTO = {
        reportingCountry: report,
        partnerCountry: partner,
        item: hs, 
        itemCost: cost
    }

    const tariffOverviewQueryDTO = {
        reportingCountry: report,
        partnerCountry: partner,
        item: hs
    }
    
    // on page load
    useEffect(() => {
        // get all country names
        const fetchCountry = async() => {
            try{
                const response = await axios.get(`http://localhost:8080/tariff/countries`); //connect to backend and do get request
                console.log(response.data);
                setList(response.data); //setting list to be data incoming from backend
                // console.log(list.country); //printing list.country should have list.code / list.short

            } catch(error){
                console.log("Error", error);
            }
        };
        fetchCountry();
    },[]);
    
    // on call
    const fetchCurrent = async() =>{
        // try catch error
        try{
            // send to query
            console.log("Sending DTO:", tariffCalculationQueryDTO);
            const response = await axios.post(`${backendURL}/tariff/current`, tariffCalculationQueryDTO); //connect to backend sending query
            console.log("Post Success", response);
            setCurrent(response.data);
            console.log(current);

        } catch(error){
            console.log("Error", error);
        }
    };

    const fetchPast = async() =>{
        // try catch error
        try{
            // send to query
            console.log("Sending DTO:", tariffCalculationQueryDTO);
            const response = await axios.post(`http://localhost:8080/tariff/past`, tariffCalculationQueryDTO); //connect to backend sending query
            console.log("Post Success", response);
            setPast(response.data);
            console.log(past);

        } catch(error){
            console.log("Error", error);
        }
    };
    
    const testPrint = async() => {
        console.log(hs);
        console.log(partner);
        console.log(report);
    };

    return(
        <div className="min-h-screen bg-gray-50 py-8">
            <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
                {/* Header */}
                <div className="text-center mb-12">
                    <h1 className="text-4xl font-bold text-gray-900 mb-4">Tariff Calculator</h1>
                    <p className="text-lg text-gray-600">Calculate import tariffs and view historical data</p>
                </div>

                {/* Form Section */}
                <div className="bg-white rounded-lg shadow-lg p-8 mb-8">
                    <h2 className="text-2xl font-semibold text-gray-800 mb-6">Calculate Tariff</h2>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
                        {/* Reporting Country Dropdown */}
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Reporting Country
                            </label>
                            <Dropdown 
                                title="Select Reporting Country"
                                options={modList}
                                onChange={e => setReport(e.code)}
                            />
                        </div>

                        {/* Partner Country Dropdown */}
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                Partner Country
                            </label>
                            <Dropdown 
                                title="Select Partner Country"
                                options={modList}
                                onChange={e => setPartner(e.code)}
                            />
                        </div>

                        {/* Item Input */}
                        <div>
                            <label htmlFor="hs" className="block text-sm font-medium text-gray-700 mb-2">
                                Item (HS Code)
                            </label>
                            <input
                                type="text"
                                id="hs"
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-colors"
                                placeholder="Enter HS code"
                                onChange={e => setHS((e.target.value).toLowerCase())}
                            />
                        </div>

                        {/* Cost Input */}
                        <div>
                            <label htmlFor="cost" className="block text-sm font-medium text-gray-700 mb-2">
                                Cost (USD)
                            </label>
                            <input
                                type="number"
                                id="cost"
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-colors"
                                placeholder="Enter cost in USD"
                                onChange={e => setCost(e.target.value)}
                            />
                        </div>
                    </div>

                    {/* Action Buttons */}
                    <div className="flex flex-wrap gap-3 mb-6">
                        <button 
                            onClick={fetchCurrent}
                            className="bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 px-6 rounded-lg transition-colors duration-200 shadow-md hover:shadow-lg"
                        >
                            Calculate Current Tariff
                        </button>
                        <button 
                            onClick={fetchPast}
                            className="bg-green-600 hover:bg-green-700 text-white font-semibold py-2 px-6 rounded-lg transition-colors duration-200 shadow-md hover:shadow-lg"
                        >
                            View Historical Data
                        </button>
                        <button 
                            onClick={testPrint}
                            className="bg-gray-600 hover:bg-gray-700 text-white font-semibold py-2 px-4 rounded-lg transition-colors duration-200 shadow-md hover:shadow-lg"
                        >
                            Debug
                        </button>
                    </div>
                </div>

                {/* Results Section */}
                {(current.tariffRate || current.tariffAmount || current.itemCostWithTariff) && (
                    <div className="bg-white rounded-lg shadow-lg p-8 mb-8">
                        <h2 className="text-2xl font-semibold text-gray-800 mb-6">Current Tariff Results</h2>
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                            <div className="bg-blue-50 p-6 rounded-lg">
                                <h3 className="text-lg font-semibold text-blue-900 mb-2">Tariff Rate</h3>
                                <p className="text-3xl font-bold text-blue-600">
                                    {current.tariffRate ? `${current.tariffRate}%` : '—'}
                                </p>
                            </div>
                            <div className="bg-green-50 p-6 rounded-lg">
                                <h3 className="text-lg font-semibold text-green-900 mb-2">Tariff Amount</h3>
                                <p className="text-3xl font-bold text-green-600">
                                    {current.tariffAmount ? `$${current.tariffAmount}` : '—'}
                                </p>
                            </div>
                            <div className="bg-purple-50 p-6 rounded-lg">
                                <h3 className="text-lg font-semibold text-purple-900 mb-2">Total Cost with Tariff</h3>
                                <p className="text-3xl font-bold text-purple-600">
                                    {current.itemCostWithTariff ? `$${current.itemCostWithTariff}` : '—'}
                                </p>
                            </div>
                        </div>
                    </div>
                )}

                {/* Chart Section */}
                {past.tariffData && past.tariffData.length > 0 && (
                    <div className="bg-white rounded-lg shadow-lg p-8">
                        <h2 className="text-2xl font-semibold text-gray-800 mb-6">Historical Tariff Data</h2>
                        <div className="bg-gray-50 p-6 rounded-lg">
                            <Chart labels={labels} value={value} title={title} legend={legend}/>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}