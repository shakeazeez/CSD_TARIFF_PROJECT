use std::env;

use actix_web::{HttpMessage, HttpRequest, HttpResponse, Responder, http::header, web};
use reqwest::Client;

use crate::{jwt::jwt_functions::Claims, tables::Role};

async fn establish_connection(
    req: &HttpRequest,
    url: &str,
    body: web::Bytes,
) -> Result<reqwest::Response, reqwest::Error> {
    let client = Client::new();
    if req.method() == actix_web::http::Method::DELETE {
        client
            .delete(url)
            .body(body)
            .header("Content-Type", "application/json")
            .send()
            .await
    } else if req.method() == actix_web::http::Method::PUT {
        client
            .put(url)
            .body(body)
            .header("Content-Type", "application/json")
            .send()
            .await
    } else if req.method() == actix_web::http::Method::POST {
        client
            .post(url)
            .body(body)
            .header("Content-Type", "application/json")
            .send()
            .await
    } else if req.method() == actix_web::http::Method::OPTIONS {
        client
            .request(actix_web::http::Method::OPTIONS, url)
            .body(body)
            .header("Content-Type", "application/json")
            .send()
            .await
    } else {
        reqwest::get(url).await
    }

}

pub async fn news_route(req: HttpRequest, body: web::Bytes) -> impl Responder {
    if req.method() == actix_web::http::Method::OPTIONS {
        return HttpResponse::Ok().finish();
    }

    let base_url = env::var("NEWS_URL").unwrap();
    let uri = req.uri().to_string();
    let url = format!("{}{}", base_url, uri);

    let response = establish_connection(&req, &url, body).await;

    match response {
        Ok(res) => {
            println!("Successful request");
            let status = res.status();
            let bytes = res.bytes().await.unwrap_or_default();
            HttpResponse::build(status)
                .insert_header((header::CONTENT_TYPE, "application/json"))
                .body(bytes)
        }
        Err(e) => {
            println!("{}", e);
            return HttpResponse::InternalServerError().finish();
        }
    }
}

pub async fn tariff_route(req: HttpRequest, body: web::Bytes) -> impl Responder {
    let base_url = env::var("TARIFF_URL").unwrap();
    let uri = req.uri().to_string();
    let url = format!("{}{}", base_url, uri);

    let response = establish_connection(&req, &url, body).await;

    match response {
        Ok(res) => {
            println!("Successful request");
            let status = res.status();
            let bytes = res.bytes().await.unwrap_or_default();
            HttpResponse::build(status)
                .insert_header((header::CONTENT_TYPE, "application/json"))
                .body(bytes)
        }
        Err(e) => {
            println!("{e}");
            HttpResponse::InternalServerError().finish()
        }
    }
}

pub async fn user_route(req: HttpRequest, body: web::Bytes) -> impl Responder {
    let extensions = req.extensions();
    if extensions
        .get::<jsonwebtoken::TokenData<Claims>>()
        .is_none()
    {
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

    if !uri.contains("user") && !uri.contains(&format!("{}", access)) {
        return HttpResponse::Forbidden().finish();
    }

    let url = format!("{}{}", base_url, uri);

    let response = establish_connection(&req, &url, body).await;

    match response {
        Ok(res) => {
            println!("Successful request");
            let status = res.status();
            let bytes = res.bytes().await.unwrap_or_default();
            HttpResponse::build(status)
                .insert_header((header::CONTENT_TYPE, "application/json"))
                .body(bytes)
        }
        Err(e) => {
            println!("{e}");
            HttpResponse::InternalServerError().finish()
        }
    }
}

pub async fn news_history(req: HttpRequest, body: web::Bytes) -> impl Responder {
    println!("This request requires token");
    let extensions = req.extensions();
    if extensions
        .get::<jsonwebtoken::TokenData<Claims>>()
        .is_none()
    {
        return HttpResponse::Unauthorized().finish();
    }

    let base_url = env::var("NEWS_URL").unwrap();
    
    let uri = req.uri().to_string();
    
    let url = format!("{}{}", base_url, uri);
    
    let response = establish_connection(&req, &url, body).await;
    
    match response {
        Ok(res) => {
            println!("Successful request");
            let status = res.status();
            let bytes = res.bytes().await.unwrap_or_default();
            HttpResponse::build(status)
                .insert_header((header::CONTENT_TYPE, "application/json"))
                .body(bytes)
        }, 
        Err(e) => {
            println!("Error: {}", e);
            return HttpResponse::InternalServerError().finish();
        } 
    }
}
