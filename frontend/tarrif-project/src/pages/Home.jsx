import axios from 'axios'
import Dropdown from '../components/Dropdown'
import Chart from '../components/Chart'
import { useEffect, useState } from 'react'


export function Home(){
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
            const response = await axios.post(`http://localhost:8080/tariff/current`, tariffCalculationQueryDTO); //connect to backend sending query
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
        <>
        <div>
            <h1>Home</h1>
            <Dropdown 
                title="Reporting Country"
                options={modList}
                onChange={e => setReport(e.code)}
            />
            <Dropdown 
                title="Partner Country"
                options={modList}
                onChange={e => setPartner(e.code)}
            />
            <p>Item:</p>
            <input
                type="text"
                id="hs"
                onChange={e => setHS((e.target.value).toLowerCase())}
            />
            <p>Cost:</p>
            <input
                type="number"
                id="cost"
                onChange={e => setCost(e.target.value)}
            /><br/>
            <button onClick={fetchCurrent}> current </button>
            <button onClick={fetchPast}> past </button>
            <button onClick={testPrint}> testPrint </button><button onClick={testPrint}> testPrint </button><button onClick={testPrint}> testPrint </button><button onClick={testPrint}> testPrint </button>

            <p>Tariff Rate:</p><p>{current.tariffRate + "%"}</p>
            <p>Tariff Amount:</p><p>{"USD" + current.tariffAmount}</p>
            <p>Total Cost with Tariff:</p><p>{"USD" + current.itemCostWithTariff}</p>
            <Chart labels={labels} value={value} title={title} legend={legend}/>
        </div>
        </>
    );
}