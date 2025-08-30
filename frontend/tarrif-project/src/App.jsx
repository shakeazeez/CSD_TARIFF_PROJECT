import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import axios from 'axios'
import React from 'react'
import './App.css'

function App() {
  const [count, setCount] = useState(0);
  const [dataset, setDataset] = useState([]);
  const WTO_KEY = import.meta.env.VITE_WTO_API_KEY;

  const fetchReview = async () => {
    try {
      const getReview = await axios.get(`https://api.wto.org/timeseries/v1/data?i=HS_A_0010&r=840&p=156&ps=2020-2022&pc=HS2&mode=full&subscription-key=${WTO_KEY}`);
      console.log("GET Success", getReview);
      setDataset(getReview.data.Dataset);
      console.log(dataset);

    } catch (error) {
      console.error("Error", error);
    }
  };

  const getIndicators = async () => {
    try {
      const getReview = await axios.get(`https://api.wto.org/timeseries/v1/indicators?i=all&t=all&pc=all&tp=all&frq=all&lang=1&subscription-key=${WTO_KEY}`);
      console.log("GET Success", getReview);
      setDataset(getReview.data);
      console.log(dataset);

    } catch (error) {
      console.error("Error", error);
    }
  };

  const getPC = async () => {
    try {
      const getReview = await axios.get(`https://api.wto.org/timeseries/v1/products?pc=all&subscription-key=${WTO_KEY}`);
      console.log("GET Success", getReview);
      setDataset(getReview.data);
      console.log(dataset);

    } catch (error) {
      console.error("Error", error);
    }
  };

  const getReporting = async () => {
    try {
      const getReview = await axios.get(`https://api.wto.org/timeseries/v1/reporters?ig=all&reg=all&gp=all&subscription-key=${WTO_KEY}`);
      console.log("GET Success", getReview);
      setDataset(getReview.data);
      console.log(dataset);

    } catch (error) {
      console.error("Error", error);
    }
  };

  const getPartner = async () => {
    try {
      const getReview = await axios.get(`https://api.wto.org/timeseries/v1/partners?ig=all&reg=all&gp=all&subscription-key=${WTO_KEY}`);
      console.log("GET Success", getReview);
      setDataset(getReview.data);
      console.log(dataset);

    } catch (error) {
      console.error("Error", error);
    }
  };

  const headers = [...new Set(dataset.flatMap(obj => Object.keys(obj)))];

  return (
    <>
      <div>
        <p>heh</p>
        <button onClick={fetchReview}>FetchReview 🔍</button>
        <button onClick={getIndicators}>Indicators 🔍</button>
        <button onClick={getPC}>PC 🔍</button>
        <button onClick={getReporting}>Reporting 🔍</button>
        <button onClick={getPartner}>Partners 🔍</button>
      </div>

      <div style={{ padding: "20px" }}>
      <h2>API Data Table</h2>
      <table style={{ borderCollapse: "collapse", width: "100%" }}>
        <thead>
          <tr>
            {headers.map((key) => (
              <th
                key={key}
                style={{
                  border: "1px solid #ddd",
                  padding: "8px",
                  backgroundColor: "#171414ff",
                  textAlign: "left"
                }}
              >
                {key}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {dataset.map((row, idx) => (
            <tr key={idx}>
              {headers.map((key) => (
                <td
                  key={key}
                  style={{
                    border: "1px solid #ddd",
                    padding: "8px"
                  }}
                >
                  {row[key] ?? ""}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
    </>
  )
}

export default App
