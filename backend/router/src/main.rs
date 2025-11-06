use std::env;

use actix_cors::Cors;
use actix_web::{
    App, HttpServer,
    web::{self},
};
use diesel::{
    Connection, PgConnection,
    r2d2::{ConnectionManager, Pool},
};
use dotenv::dotenv;
use utoipa::OpenApi;
use utoipa_swagger_ui::SwaggerUi;

use crate::jwt::jwt_middleware::JwtMiddleware;

mod auth;
mod dto;
mod encryption;
mod jwt;
mod router;
mod schema;
mod tables;
mod docs;

type DbPool = Pool<ConnectionManager<PgConnection>>;

fn establish_connection() -> Pool<ConnectionManager<PgConnection>> {
    let database_url = env::var("RUST_DATABASE_URL").expect("Database url not set");

    let manager = ConnectionManager::<PgConnection>::new(&database_url);
    let pool = DbPool::builder().build(manager).unwrap();

    PgConnection::establish(&database_url)
        .unwrap_or_else(|_| panic!("Unable to connect to the database"));

    pool
}

fn init_news(cfg: &mut web::ServiceConfig) {
    cfg.service(web::scope("/news").route("/{tail:.*}", web::to(router::news_route)));
}

fn init_login(cfg: &mut web::ServiceConfig) {
    cfg.service(
        web::scope("/auth")
            .route("/login", web::post().to(auth::login))
            .route("/register", web::post().to(auth::create_user)),
    );
}

fn init_tariff(cfg: &mut web::ServiceConfig) {
    cfg.service(web::scope("/tariff").route("/{tail:.*}", web::to(router::tariff_route)));
}

fn init_user(cfg: &mut web::ServiceConfig) {
    cfg.service(
        web::scope("/{prefix:(user|member|business|bank)}")
            .wrap(JwtMiddleware)
            .route("/{tail:.*}", web::to(router::user_route)),
    );
}

fn init_news_history(cfg: &mut web::ServiceConfig) {
    cfg.service(
        web::scope("/news/history")
            .wrap(JwtMiddleware)
            .route("/{tail:.*}", web::to(router::news_history)),
    );
}

#[actix_web::main]
async fn main() {
    dotenv().ok();
    let connection = establish_connection();

    println!("Hello new Server application");
    let mut host = env::var("HOST").unwrap_or_else(|_| "0.0.0.0:8080".to_string());
    let openapi = docs::ApiDoc::openapi();
    
    // For local testing purposes
    if host == "127.0.0.1:8080" {
        host = "0.0.0.0:8080".to_string();
    }
    // println!("Host: {host}");
    
    println!("Host : {host}");
    HttpServer::new(move || {
        let cors = Cors::default()
            .allowed_origin(
                &env::var("FRONTEND_URL").unwrap_or_else(|_| {
                    println!("Defaulting");
                    "0.0.0.1:80".to_string()
                }),
            )
            .allow_any_header()
            .allow_any_method();

        App::new()
            .wrap(cors)
            .app_data(web::Data::new(connection.clone()))
            .service(SwaggerUi::new("/swagger-ui/{_:.*}").url("/api-docs/openapi.json", openapi.clone()))
            .configure(init_news_history)
            .configure(init_news)
            .configure(init_login)
            .configure(init_tariff)
            .configure(init_user)
    })
    .bind(host)
    .unwrap()
    .run()
    .await
    .unwrap();
    println!("Ended process");
}
