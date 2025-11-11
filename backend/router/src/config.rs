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

use crate::DbPool;
use crate::jwt::jwt_middleware::JwtMiddleware;
use crate::router;
use crate::docs;
use crate::auth;

/*
 * Initialises c‹⁄onnection to database
 *
 * @Return -> Pointer to the connected database
 */
pub fn establish_connection() -> Pool<ConnectionManager<PgConnection>> {
    let database_url = env::var("RUST_DATABASE_URL").unwrap_or_default();

    let manager = ConnectionManager::<PgConnection>::new(&database_url);
    let pool = DbPool::builder().build(manager).unwrap();

    PgConnection::establish(&database_url)
        .unwrap_or_else(|_| panic!("Unable to connect to the database"));

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