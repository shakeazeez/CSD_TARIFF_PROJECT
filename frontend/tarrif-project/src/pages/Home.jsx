import Select from 'react-select'
import axios from 'axios'
import Dropdown from "../components/Dropdown";
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
    // testing list - ends here

    const tariffCalculationQueryDTO = {
        reportingCountry: report,
        partnerCountry: partner,
        item: hs, 
        itemCost: 1000.0,
        effectiveDate: null
    }

    const tariffRespondDTO = {
        reportingCountry: report,
        partnerCountry: partner,
        item: hs,
        tariffRate: null,
        tariffAmount: null,
        itemCostWithTariff: null
    };

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

    },[]);
    
    // on call
    const fetch = async() =>{
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
            <button onClick={fetch}> Search </button>
        </div>
        </>
    );
}