use std::{collections::HashSet, env, time::UNIX_EPOCH};

use actix_web::{cookie::time::{ext::SystemTimeExt, Duration}};
use jsonwebtoken::{errors::{Error}, jws::encode, DecodingKey, EncodingKey, Header, TokenData, Validation};
use serde::{Deserialize, Serialize};


use crate::tables::{AuthUser, Role};

/*
 * This is an object that contains all the necessary claims to be 
 * stored during creation of the JWT token. 
 * 
 * @Param custom_claims -> Indicates reason token is created 
 * @Param iss           -> Issuer of token (User URL)
 * @Param exp           -> How long each token lasts for (1 day as standard implementation)
 * @Param sub           -> Contains login username 
 * @Param groups        -> Role of the user
 * @Param aud           -> Who owns the spring 
 */
#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Claims {
    custom_claims: String,
    iss: String,
    exp: u64,
    sub: String,
    pub groups: Role,
    aud: String,
}

/*
 * Validates whether the JSON Token is a valid token and not something randomly sent in by user
 * 
 * @Param token -> A string token from the request
 * @Return      -> A result struct that either contains the verified token or a jwtwebtoken error
 *                 if is an invalid token. 
 */
pub fn verify_jwt(token: String) -> Result<TokenData<Claims>, Error> {
    let mut validation = Validation::new(jsonwebtoken::Algorithm::HS512);
    validation.required_spec_claims = HashSet::new();
    validation.validate_aud = false;
    let secret = DecodingKey::from_secret(env::var("SIGNING_SECRET").unwrap().as_bytes());

    jsonwebtoken::decode::<Claims>(&token, &secret, &validation)
}

/*
 * Generates the token for the user 
 * 
 * @Param user  -> This is the orm containing username and password
 * @Param roles -> This is the orm contaning the relationship between user 
 *                 and role
 * 
 * Return       -> String that contains the raw token 
 */
pub fn generate_token(user: &AuthUser) -> String {
    let now_plus_60 = std::time::SystemTime::now()
        .checked_add_signed(Duration::days(1))
        .expect("System time break at duration add by 1 day")
        .duration_since(UNIX_EPOCH)
        .expect("System time break at duration UNIX EPOCJ")
        .as_secs();

    let created_claims =
        Claims {
            custom_claims: "Logging in".to_owned(),
            iss: env::var("USER_URL").unwrap_or_default(),
            exp: now_plus_60,
            sub: user.username.to_owned(),
            groups: user.user_roles,
            aud: "frontend".to_owned()
        };

    let header = Header::new(jsonwebtoken::Algorithm::HS512);
    let secret = EncodingKey::from_secret(env::var("SIGNING_SECRET").unwrap().as_bytes());

    let res = encode(&header, Some(&created_claims), &secret).unwrap();
    format!("{}.{}.{}", res.protected, res.payload, res.signature)
}
