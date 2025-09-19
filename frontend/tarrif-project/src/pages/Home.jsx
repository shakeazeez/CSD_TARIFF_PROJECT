import axios from 'axios'
import Dropdown from '../components/Dropdown'
import Chart from '../components/Chart'
import { useEffect, useState } from 'react'


export function Home(){
    // const [list, setList] = useState([]);
    const [report, setReport] = useState("China");
    const [partner, setPartner] = useState("Singapore");
    const [hs, setHS] = useState("Slipper");
    
    // testing list - start here
    const list = [
        {country : "Afghanistan", code : "004"},
        {country : "Albania", code : "008"},
        {country : "Andorra", code : "020"},
        {country : "Angola", code : "024"},
    ];

    const modList = list.map(item => ({
        id: item.country,
        code: item.code
    }));

    const hscode = [
        {stuff : "Some stuff1", code : "HS100001"},
        {stuff : "Some stuff2", code : "HS100002"},
        {stuff : "Some stuff3", code : "HS100003"},
        {stuff : "Some stuff4", code : "HS100004"}
    ];

    const modhs = hscode.map(item => ({
        id: item.stuff,
        code: item.code
    }));

    const value = [[200, 100, 600, 1000]];
    const labels = ["Jan", "Feb", "Mar", "Apr"];
    const legend = ["Shop 1"];
    const title = "Total sales";
    // testing list - ends here

    const tariffCalculationQueryDTO = {
        reportingCountry: report,
        partnerCountry: partner,
        item: hs, 
        itemCost: 1000.0,
        effectiveDate: null
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
                const response = await axios.get(`${backendUrl}/`); //connect to backend and do get request
                setList(response.data); //setting list to be data incoming from backend
                console.log(list.country); //printing list.country should have list.code / list.short

            } catch(error){
                console.log("Error", error);
            }
        };
        // fetchCountry();
    },[]);
    
    // on call
    const fetchCurrent = async() =>{
        // try catch error
        try{
            // send to query
            console.log("Sending DTO:", tariffCalculationQueryDTO);
            const response = await axios.post(`http://localhost:8080/tariff/current`, tariffCalculationQueryDTO); //connect to backend sending query
            console.log("Post Success", response);

        } catch(error){
            console.log("Error", error);
        }
    };

    const fetchPast = async() =>{
        // try catch error
        try{
            // send to query
            console.log("Sending DTO:", tariffOverviewQueryDTO);
            const response = await axios.post(`http://localhost:8080/tariff/past`, tariffOverviewQueryDTO); //connect to backend sending query
            console.log("Post Success", response);

        } catch(error){
            console.log("Error", error);
        }
    };

    const printout = async() => {
        console.log(report);
        console.log(partner);
        console.log(hs);
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
            <Dropdown 
                title="HS Code"
                options={modhs}
                onChange={e => setHS(e.code)}
            />
            <button onClick={fetchCurrent}> current </button>
            <button onClick={fetchPast}> past </button>
            <Chart labels={labels} value={value} title={title} legend={legend}/>
        </div>
        </>
    );
}