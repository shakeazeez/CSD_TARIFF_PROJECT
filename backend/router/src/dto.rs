use serde::{Deserialize, Serialize};


#[derive(Serialize, Deserialize, Clone)]
pub struct LoginDTO {
    pub username: String,
    pub password: String
}

#[derive(Serialize, Deserialize, Clone)]
pub struct CreateDTO {
    pub username: String, 
    pub password: String,
    pub role: String,
    pub industry: String, 
    pub originCountry: String,
    pub destinationCountries: Vec<String>,
    pub itemsSold: Vec<String>
}

#[derive(Serialize, Deserialize, Clone)]
pub struct TokenDTO {
    username: String,
    pub token: String,
    pin: Option<Vec<i32>>,
    industry: Option<String>,
    origin_country: Option<String>,
    destination_countries: Option<Vec<i32>>,
    items_sold: Option<String>,
    historical_tariff_id: Option<i32>
}