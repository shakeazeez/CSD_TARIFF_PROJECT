use std::{collections::HashSet, env, time::UNIX_EPOCH};

use actix_web::{cookie::time::{ext::SystemTimeExt, Duration}};
use jsonwebtoken::{errors::{Error}, jws::encode, DecodingKey, EncodingKey, Header, TokenData, Validation};
use serde::{Deserialize, Serialize};


use crate::tables::{AuthUser, Role, UserRole};

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Claims {
    custom_claims: String,
    iss: String,
    sub: String,
    aud: String,
    pub groups: Role,
    exp: u64
}

pub fn verify_jwt(token: String) -> Result<TokenData<Claims>, Error> {
    let mut validation = Validation::new(jsonwebtoken::Algorithm::HS512);
    validation.required_spec_claims = HashSet::new();
    validation.validate_aud = false;

    let secret = DecodingKey::from_secret(env::var("SIGNING_SECRET").unwrap().as_bytes());

    let res = jsonwebtoken::decode::<Claims>(&token, &secret, &validation);
    
    res
}

pub fn generate_token(user: &AuthUser, roles: &UserRole) -> String {
    let now_plus_60 = std::time::SystemTime::now()
        .checked_add_signed(Duration::days(1))
        .expect("System time break at duration add by 1 day")
        .duration_since(UNIX_EPOCH)
        .expect("System time break at duration UNIX EPOCJ")
        .as_secs();

    let created_claims =
        Claims {
            custom_claims: "Logging in".to_owned(),
            iss: env::var("USER_URL").unwrap(),
            exp: now_plus_60,
            sub: user.username.clone(),
            groups: roles.user_roles,
            aud: "frontend".to_owned()
        };

    let header = Header::new(jsonwebtoken::Algorithm::HS512);
    let secret = EncodingKey::from_secret(env::var("SIGNING_SECRET").unwrap().as_bytes());

    let res = encode(&header, Some(&created_claims), &secret).unwrap();
    format!("{}.{}.{}", res.protected, res.payload, res.signature)
}
