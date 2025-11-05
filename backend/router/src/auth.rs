use std::env;

use actix_web::{HttpResponse, Responder, web};
use diesel::{
    PgConnection,
    prelude::*,
    r2d2::{ConnectionManager, Pool},
};
use reqwest::Client;

use crate::{
    dto::{CreateDTO, LoginDTO, TokenDTO},
    encryption::{encrypt_password, verify_password},
    jwt::jwt_functions,
    schema::{user::dsl::*, user_role::dsl::*},
    tables::{AuthUser, Role, UserRole},
};

/*
 * Requests the user_details table 
 * 
 * @Param database -> Pointer to database conenction 
 * @Param username -> username of user
 * 
 * Return          -> ORM of the user table
 */
async fn get_user_details(
    database: &web::Data<Pool<ConnectionManager<PgConnection>>>,
    name: &String,
) -> Vec<AuthUser> {
    let mut connection = database.get().unwrap();

    let result = user
        .filter(username.eq(name))
        .select(AuthUser::as_select())
        .load::<AuthUser>(&mut connection)
        .expect("Whoops");

    return result;
}

/*
 * Requests the user_role table 
 * 
 * @Param database -> Pointer to database conenction 
 * @Param id       -> Id of user 
 * 
 * Return          -> ORM of the role table
 */
async fn get_user_roles(
    database: &web::Data<Pool<ConnectionManager<PgConnection>>>,
    id_num: i32,
) -> Vec<UserRole> {
    let mut connection = database.get().unwrap();

    let result = user_role
        .filter(user_id.eq(id_num))
        .select(UserRole::as_select())
        .load::<UserRole>(&mut connection)
        .expect("Whoops");

    return result;
}

/*
 * Allows user to login to service
 * 
 * @Param database      -> The pointer of the database connection
 * @Param login_details -> The details of the user (username and password)
 */
#[utoipa::path(
    post,
    path="/auth/login",
    request_body=LoginDTO,
    responses(
        (status = 200, description = "Login successful"),
        (status = 403, description = "Unauthorized accesss attempted"),
        (status = 404, description = "Resource not found for particular user type"),
        (status = 500, description = "Internal server error")
    )
)]
pub async fn login(
    database: web::Data<Pool<ConnectionManager<PgConnection>>>,
    login_details: web::Json<LoginDTO>,
) -> impl Responder {
    let name = login_details.username.as_deref().unwrap();
    let acc: Vec<AuthUser> = get_user_details(&database, &name.to_string()).await;
    
    if acc.len() != 1 {
        return HttpResponse::InternalServerError()
            .json("There is more than one user with the same username");
    }

    let roles = get_user_roles(&database, acc[0].id).await;
    // println!("{:?}", acc[0]);
    let password = login_details.password.as_deref().unwrap();
    match verify_password(&acc[0].hashedpassword, password.to_string()) {
        Ok(_) => {
            let generated_token = jwt_functions::generate_token(&acc[0], &roles[0]);

            // Write function to get info from user back here
            let user_url = env::var("USER_URL").unwrap();
            let response = match roles[0].user_roles {
                Role::MEMBER => {
                        reqwest::get(format!("{}/member/{}", user_url, acc[0].username).as_str())
                            .await
                            .unwrap()
                }
                Role::BUSINESS => {
                        reqwest::get(format!("{}/business/{}", user_url, acc[0].username).as_str())
                            .await
                            .unwrap()
                }
                Role::BANK => {
                        reqwest::get(format!("{}/bank/{}", user_url, acc[0].username).as_str())
                            .await
                            .unwrap()
                }
                Role::ADMIN => {
                        reqwest::get(format!("{}/user/{}", user_url, acc[0].username).as_str())
                            .await
                            .unwrap()
                }
            };
            let status = response.status();
            let body = response.text().await.unwrap();
            // println!("Failed due to some reason {}", status);
            if status.is_success() {
                let mut token_dto: TokenDTO = serde_json::from_slice(&body.into_bytes()).unwrap();
                token_dto.token = Some(generated_token);
                token_dto.username = Some(acc[0].username.to_owned());
                return HttpResponse::build(status).json(token_dto);
            }
            return HttpResponse::build(status).json(body);
        }
        Err(_) => return HttpResponse::Unauthorized().finish(),
    };
}

/*
 * Allows user to register to service
 * 
 * @Param database      -> The pointer of the database connection
 * @Param login_details -> The details of the user 
 */
#[utoipa::path(
    post,
    path="/auth/register",
    request_body=CreateDTO,
    responses(
        (status = 200, description = "Created user successfully"),
        (status = 400, description = "Request failed due to bad password or bad username"),
        (status = 409, description = "Username already exits"),
        (status = 500, description = "Internal server error")
    )
)]
pub async fn create_user(
    database: web::Data<Pool<ConnectionManager<PgConnection>>>,
    login_details: web::Json<CreateDTO>,
) -> impl Responder {
    println!("{login_details:?}");
    let mut borrow = login_details.into_inner();
    borrow.password = encrypt_password(borrow.password.clone());
    println!("{:?}", borrow);
    let client = Client::new();
    let user_url = env::var("USER_URL").unwrap();
    let req = client
        .post(format!("{}/auth/register", user_url).as_str())
        .json(&borrow)
        .send()
        .await;

    match req {
        Ok(res) => {
            println!("Successful request");
            let status = res.status();
            let body = res.text().await.unwrap();

            if body.is_empty() {
                return HttpResponse::build(status).finish();
            }

            let acc = get_user_details(&database, &borrow.username).await;
            let roles = get_user_roles(&database, acc[0].id).await;
            // println!("{body}");
            let mut token_dto: TokenDTO = serde_json::from_slice(&body.into_bytes()).unwrap();
            token_dto.token = Some(jwt_functions::generate_token(&acc[0], &roles[0]));
            // println!("{:?}", token_dto);
            println!("Status: {}", status);
            HttpResponse::build(status).json(token_dto)
        }
        Err(e) => {
            println!("Error: {e}");
            HttpResponse::InternalServerError().finish()
        }
    }
}
