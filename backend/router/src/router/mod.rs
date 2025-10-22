use std::{env, fmt};

use actix_web::{web, HttpMessage, HttpRequest, HttpResponse, Responder};
use reqwest::Client;

use crate::{jwt::jwt_functions::Claims, tables::Role};

pub async fn tariff_route(req :HttpRequest, body: web::Bytes) -> impl Responder {
    let base_url = env::var("TARIFF_URL").unwrap();
    let uri = req.uri().to_string();
    let url = format!("{}{}", base_url, uri);
    
    let client = Client::new();
    let response = if req.method() == actix_web::http::Method::GET {
        reqwest::get(url.as_str()).await
    } else {
        client.post(url.as_str())
            .body(body)
            .header("Content-Type", "application/json")
            .send()
            .await
    };
    
    
    match response {
        Ok(res) => {
            println!("Successful request");
            let status = res.status();
            let bytes = res.bytes().await.unwrap_or_default();
            HttpResponse::build(status).body(bytes.to_vec())
        }, 
        Err(e) => {
            println!("{e}");
            HttpResponse::InternalServerError().finish()
        }
    }
    
}


pub async fn user_route(req: HttpRequest, body: web::Bytes) -> impl Responder {
    let extensions = req.extensions();
    if extensions.get::<jsonwebtoken::TokenData<Claims>>().is_none() {
        return HttpResponse::Unauthorized().finish();
    }
    
    let access = extensions.get::<Role>();
    
    if access.is_none() {
        println!("ROLE is not present. FIX THIS");
        return HttpResponse::InternalServerError().finish();
    }
    
    let access = access.unwrap();
    
    let base_url = env::var("USER_URL").unwrap();
    let uri = req.uri().to_string();
    
    if !uri.contains(&format!("{}", access)) {
        return HttpResponse::Forbidden().finish()
    } 
    
    let url = format!("{}{}", base_url, uri);
    
    let client = Client::new();
    let response = if req.method() == actix_web::http::Method::GET {
        reqwest::get(url.as_str()).await
    } else {
        client.post(url.as_str())
            .body(body)
            .header("Content-Type", "application/json")
            .send()
            .await
    };
    
    
    match response {
        Ok(res) => {
            println!("Successful request");
            let status = res.status();
            let bytes = res.bytes().await.unwrap_or_default();
            HttpResponse::build(status).body(bytes.to_vec())
        }, 
        Err(e) => {
            println!("{e}");
            HttpResponse::InternalServerError().finish()
        }
    }
}