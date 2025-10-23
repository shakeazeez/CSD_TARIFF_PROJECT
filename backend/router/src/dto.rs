use serde::{Deserialize, Serialize};


#[derive(Serialize, Deserialize, Clone)]
pub struct LoginDTO {
    pub username: String,
    pub password: String
}

#[derive(Serialize, Deserialize, Clone, Debug)]
pub struct CreateDTO {
    pub username: String, 
    pub password: String,
    pub role: String,
    pub industry: Option<String>, 
    #[serde(rename = "originCountry")]
    pub origin_country: Option<String>,
    #[serde(rename = "destinationCountries")]
    pub destination_countries: Option<Vec<String>>,
    #[serde(rename = "itemsSold")]
    pub items_sold: Option<Vec<String>>
}

#[derive(Serialize, Deserialize, Clone, Debug)]
pub struct TokenDTO {
    username: Option<String>,
    pub token: Option<String>,
    #[serde(rename = "pin")]
    pin: Option<Vec<i32>>,
    industry: Option<String>,
    #[serde(rename = "originCountry")]
    origin_country: Option<String>,
    #[serde(rename = "destinationCountries")]
    destination_countries: Option<Vec<String>>,
    #[serde(rename = "itemsSold")]
    items_sold: Option<String>,
    #[serde(rename = "historicalTariffId")]
    historical_tariff_id: Option<Vec<i32>>
}