use std::env;
use actix_web::{
    web::{self},
};
use diesel::{
    Connection, PgConnection,
    r2d2::{ConnectionManager, Pool},
};
use utoipa::OpenApi;
use utoipa_swagger_ui::SwaggerUi;

use crate::jwt::jwt_middleware::JwtMiddleware;
use crate::router;
use crate::docs;
use crate::auth;

/*
 * Initialises connection to database
 *
 * @Return -> Pointer to the connected database
 */
pub fn establish_connection() -> Pool<ConnectionManager<PgConnection>> {
    let database_url = env::var("RUST_DATABASE_URL").expect("Database url not set");

    let manager = ConnectionManager::<PgConnection>::new(&database_url);
    let pool = match Pool::builder()
        .max_size(3) // Reduced to 3 connections to avoid overwhelming RDS
        .min_idle(Some(1)) // Keep at least 1 idle connection
        .max_lifetime(Some(std::time::Duration::from_secs(300))) // 5 minutes max lifetime
        .idle_timeout(Some(std::time::Duration::from_secs(60))) // Close idle connections after 1 minute
        .connection_timeout(std::time::Duration::from_secs(10)) // 10 second connection timeout
        .build(manager) {
        Ok(pool) => {
            println!("Database connection pool created successfully");
            pool
        },
        Err(e) => {
            panic!("Unable to connect to the database: {}", e);
        }
    };

    // Test the connection - don't panic on failure, just log
    match PgConnection::establish(&database_url) {
        Ok(_) => println!("Database connection established successfully"),
        Err(e) => {
            println!("Warning: Could not establish initial database connection: {}", e);
            println!("The application will continue, but database operations may fail until connection is restored");
        },
    }

    pool
}

pub fn init_news(cfg: &mut web::ServiceConfig) {
    cfg.service(web::scope("/news").route("/{tail:.*}", web::to(router::news_route)));
}

pub fn init_swagger(cfg: &mut web::ServiceConfig) {
    let openapi = docs::ApiDoc::openapi();
    cfg.service(
        SwaggerUi::new("/swagger-ui/{_:.*}").url("/api-docs/openapi.json", openapi),
    );
}

pub fn init_login(cfg: &mut web::ServiceConfig) {
    cfg.service(
        web::scope("/auth")
            .route("/login", web::post().to(auth::login))
            .route("/register", web::post().to(auth::create_user)),
    );
}

pub fn init_tariff(cfg: &mut web::ServiceConfig) {
    cfg.service(web::scope("/tariff").route("/{tail:.*}", web::to(router::tariff_route)));
}

pub fn init_user(cfg: &mut web::ServiceConfig) {
    cfg.service(
        web::scope("/{prefix:(user|member|business|bank)}")
            .wrap(JwtMiddleware)
            .route("/{tail:.*}", web::to(router::user_route)),
    );
}

pub fn init_news_history(cfg: &mut web::ServiceConfig) {
    cfg.service(
        web::scope("/news/history")
            .wrap(JwtMiddleware)
            .route("/{tail:.*}", web::to(router::news_history)),
    );
}