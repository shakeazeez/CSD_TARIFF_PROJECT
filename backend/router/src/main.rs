use std::env;

use actix_cors::Cors;
use actix_governor::{Governor, GovernorConfigBuilder};
use actix_web::{
    App, HttpServer,
    web::{self},
};
use diesel::{
    PgConnection,
    r2d2::{ConnectionManager, Pool},
};
use dotenv::dotenv;

mod auth;
mod docs;
mod dto;
mod encryption;
mod jwt;
mod router;
mod schema;
mod tables;
mod config;

type DbPool = Pool<ConnectionManager<PgConnection>>;



#[actix_web::main]
async fn main() {
    dotenv().ok();
    let connection = config::establish_connection();

    println!("Hello new Server application");
    let host = env::var("HOST").unwrap_or_else(|_| "127.0.0.1:8080".to_string());

    let governor_conf = GovernorConfigBuilder::default()
        .const_burst_size(20)
        .const_milliseconds_per_request(10)
        .finish()
        .unwrap();

    HttpServer::new(move || {
        let cors = Cors::default()
            .allowed_origin(
                &env::var("FRONTEND_URL").unwrap_or_else(|_| "127.0.0.1:80".to_string()),
            )
            .allow_any_header()
            .allow_any_method();

        App::new()
            .wrap(cors)
            .wrap(Governor::new(&governor_conf))
            .app_data(web::Data::new(connection.clone()))
            .configure(config::init_swagger)
            .configure(config::init_news_history)
            .configure(config::init_news)
            .configure(config::init_login)
            .configure(config::init_tariff)
            .configure(config::init_user)
    })
    .bind(host)
    .unwrap()
    .run()
    .await
    .unwrap();
}
