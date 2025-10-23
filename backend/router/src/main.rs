use std::env;

use actix_web::{web::{self}, App, HttpServer};
use diesel::{r2d2::{ConnectionManager, Pool}, Connection, PgConnection};
use dotenv::dotenv;

use crate::jwt::jwt_middleware::JwtMiddleware;

mod jwt;
mod tables;
mod schema;
mod auth;
mod dto;
mod encryption;
mod router;

type DbPool = Pool<ConnectionManager<PgConnection>>;

pub fn establish_connection() -> Pool<ConnectionManager<PgConnection>> {
    let database_url = env::var("DATABASE_URL").expect("Database url not set");
    
    let manager = ConnectionManager::<PgConnection>::new(&database_url);
    let pool = DbPool::builder().build(manager).unwrap();
    
    PgConnection::establish(&database_url)
        .unwrap_or_else(|_| panic!("Unable to connect to the database")
    );
    
    pool
}

pub fn init_login(cfg: &mut web::ServiceConfig) {
    cfg.service (
        web::scope("/auth")
            .route("/login", web::post().to(auth::login))
            .route("/register", web::post().to(auth::create_user))
    );
}

pub fn init_tariff(cfg: &mut web::ServiceConfig) {
    cfg.service(
        web::scope("/tariff")
            .route("/{tail:.*}", web::to(router::tariff_route))
    );
}

pub fn init_user(cfg: &mut web::ServiceConfig) {
    cfg.service(
        web::scope("/{prefix:(user|member|business|bank)}")
            .wrap(JwtMiddleware)
            .route("/{tail:.*}", web::to(router::user_route))
    );
}

#[actix_web::main]
async fn main() {
    
    env_logger::init();
    dotenv().ok();
    let connection = establish_connection();
    
    println!("Hello new Server application");
    let host = env::var("HOST").unwrap_or_else(|_| "127.0.0.1:8080".to_string());
    
    HttpServer::new(move || {
        App::new()
            .app_data(web::Data::new(connection.clone()))
            .configure(init_login)
            .configure(init_tariff)
            .configure(init_user)
    })
    .bind(host)
    .unwrap()
    .run()
    .await
    .unwrap();
    
    
    
}
