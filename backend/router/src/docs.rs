use utoipa::OpenApi;


#[derive(OpenApi)]
#[openapi(
    tags(
        (name = "RUST CRUD API",description="RUST Actix-web and SQLX CRUD API")
    ),
    paths(
        crate::auth::login,
        crate::auth::create_user
    )
)]
pub struct ApiDoc;