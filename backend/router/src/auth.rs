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

pub async fn login(
    database: web::Data<Pool<ConnectionManager<PgConnection>>>,
    login_details: web::Json<LoginDTO>,
) -> impl Responder {
    let acc: Vec<AuthUser> = get_user_details(&database, &login_details.username).await;

    if acc.len() != 1 {
        return HttpResponse::InternalServerError()
            .json("There is more than one user with the same username");
    }

    let roles = get_user_roles(&database, acc[0].id).await;
    println!("{:?}", acc[0]);
    match verify_password(&acc[0].hashedpassword, login_details.password.clone()) {
        Ok(_) => {
            let generated_token = jwt_functions::generate_token(&acc[0], &roles[0]);

            // Write function to get info from user back here
            let user_url = env::var("USER_URL").unwrap();
            let response;
            match roles[0].user_roles {
                Role::MEMBER => {
                    response =
                        reqwest::get(format!("{}/member/{}", user_url, acc[0].username).as_str())
                            .await
                            .unwrap()
                }
                Role::BUSINESS => {
                    response =
                        reqwest::get(format!("{}/business/{}", user_url, acc[0].username).as_str())
                            .await
                            .unwrap()
                }
                Role::BANK => {
                    response =
                        reqwest::get(format!("{}/bank/{}", user_url, acc[0].username).as_str())
                            .await
                            .unwrap()
                }
                Role::ADMIN => {
                    response =
                        reqwest::get(format!("{}/user/{}", user_url, acc[0].username).as_str())
                            .await
                            .unwrap()
                }
            }
            let status = response.status();
            let body = response.text().await.unwrap();
            if status.is_success() {
                let mut token_dto: TokenDTO = serde_json::from_slice(&body.into_bytes()).unwrap();
                token_dto.token = Some(generated_token);
                return HttpResponse::build(status).json(token_dto);
            }
            return HttpResponse::build(status).json(body);
        }
        Err(_) => return HttpResponse::Unauthorized().finish(),
    };
}

pub async fn create_user(
    database: web::Data<Pool<ConnectionManager<PgConnection>>>,
    login_details: web::Json<CreateDTO>,
) -> impl Responder {
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
            return HttpResponse::build(status).json(token_dto);
        }
        Err(e) => {
            println!("Error: {e}");
            return HttpResponse::InternalServerError().finish();
        }
    }
}
