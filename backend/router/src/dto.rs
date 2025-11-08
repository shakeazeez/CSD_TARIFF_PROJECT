use indexmap::IndexMap;
use serde::{Deserialize, Serialize};
use utoipa::ToSchema;


#[derive(Serialize, Deserialize, Clone, Debug, ToSchema)]
pub struct LoginDTO {
    pub username: Option<String>,
    pub password: Option<String>
}

#[derive(Serialize, Deserialize, Clone, Debug, ToSchema)]
pub struct BusinessDetailsDTO {
    #[serde(rename = "reportingCountry")]
    pub reporting_country: String,
    #[serde(rename = "item")]
    pub item_name: String
}

#[derive(Serialize, Deserialize, Clone, Debug, ToSchema)]
pub struct CreateDTO {
    pub username: String, 
    pub password: String,
    pub role: String,
    pub industry: Option<String>, 
    #[serde(rename = "tariffs")]
    pub tariffs: Option<Vec<BusinessDetailsDTO>>,
    #[serde(rename = "itemsSold")]
    pub items_sold: Option<Vec<String>>,
    #[serde(rename = "originCountry")]
    pub origin_country: Option<String>
}

#[derive(Serialize, Deserialize, Clone, Debug)]
pub struct TokenDTO {
    pub username: Option<String>,
    pub token: Option<String>,
    pub role: Option<String>,
    #[serde(rename = "pin")]
    pin: Option<Vec<i32>>,
    industry: Option<String>,
    #[serde(rename = "originCountry")]
    origin_country: Option<String>,
    #[serde(rename = "tariffs")]
    tariffs: Option<Vec<BusinessDetailsDTO>>,
    #[serde(rename = "historicalTariffId")]
    historical_tariff_id: Option<IndexMap<i32, String>>
}