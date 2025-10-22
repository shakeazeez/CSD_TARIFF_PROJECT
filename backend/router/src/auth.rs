use std::env;

use actix_web::{web, HttpResponse, Responder};
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
    schema::{user_role::dsl::*, user::dsl::*},
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
    login_details: web::Data<LoginDTO>,
) -> impl Responder {
    let acc: Vec<AuthUser> = get_user_details(&database, &login_details.username).await;

    if acc.len() != 1 {
        return HttpResponse::InternalServerError()
            .json("There is more than one user with the same username");
    }

    let roles = get_user_roles(&database, acc[0].id).await;

    match verify_password(&acc[0].hashed_password, login_details.password.clone()) {
        Ok(_) => {
            let generated_token = jwt_functions::generate_token(&acc[0], &roles[0]);
            
            // Write function to get info from user back here
            let user_url = env::var("USER_URL").unwrap();
            let response;
            match Role::from_i32(roles[0].user_id) {
                Role::MEMBER => {
                    response = reqwest::get(format!("{}/member/{}", user_url, acc[0].username).as_str())
                        .await.unwrap()
                }, 
                Role::BUSINESS => {
                    response = reqwest::get(format!("{}/business/{}", user_url, acc[0].username).as_str())
                        .await.unwrap()
                },
                Role::BANK => {
                    response = reqwest::get(format!("{}/bank/{}", user_url, acc[0].username).as_str())
                        .await.unwrap()
                },
                Role::ADMIN => {
                    response = reqwest::get(format!("{}/user/{}", user_url, acc[0].username).as_str())
                        .await.unwrap()
                }
            }
            let mut res: TokenDTO = response.json().await.unwrap();
            res.token = generated_token;
            return HttpResponse::Ok().json(res);
        }
        Err(_) => return HttpResponse::Unauthorized().finish(),
    };
}


pub async fn create_user (
    database: web::Data<Pool<ConnectionManager<PgConnection>>>,
    login_details: web::Data<CreateDTO>,
) -> impl Responder
{
    let mut borrow = login_details.as_ref().clone();
    borrow.password = encrypt_password(borrow.password.clone());
    
    let client = Client::new();
    let user_url = env::var("USER_URL").unwrap();
    let req = client.post(format!("{}/auth/register", user_url).as_str())
        .json(&borrow)
        .send()
        .await;
    
    match req {
        Ok(req) => {
            let mut res: TokenDTO = req.json().await.unwrap();
            let acc = get_user_details(&database, &borrow.username).await;
            let roles = get_user_roles(&database, acc[0].id).await;
            res.token = jwt_functions::generate_token(&acc[0], &roles[0]);
            return HttpResponse::Ok().json(res);
        }, 
        Err(e) => {
            println!("{e}");
            return HttpResponse::InternalServerError().finish();
        }
    }
}