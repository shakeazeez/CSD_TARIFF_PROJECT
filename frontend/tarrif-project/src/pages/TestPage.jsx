import { SearchCard } from "../components/SearchCard";
import { useState, useEffect } from "react";
import axios from "axios";


export function TestPage(){
    const backendURL = import.meta.env.VITE_BACKEND_URL;

    const [list, setList] = useState([]);

    // on page load
    useEffect(() => {
        // get all country names
        const fetchCountry = async() => {
            try{
                const response = await axios.get(`${backendURL}/tariff/countries`); //connect to backend and do get request
                console.log(response.data);
                setList(response.data); //setting list to be data incoming from backend

            } catch(error){
                console.log("Error", error);
            }
        };
        fetchCountry();
    },[]);

    return(
        <>
        <div>
            <SearchCard/>
        </div>
        </>
    );
}