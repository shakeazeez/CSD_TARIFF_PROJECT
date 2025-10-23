use argon2::{password_hash::{{rand_core::OsRng, SaltString}, Error}, Argon2, Params, PasswordHash, PasswordHasher, PasswordVerifier};

pub fn encrypt_password(password: String) -> String {
    let salt = SaltString::generate(&mut OsRng);
    
    let hash = Argon2::new(
        argon2::Algorithm::Argon2id,
        argon2::Version::V0x13,
        Params::new(15000, 2, 1, None)
            .unwrap_or_else(|e| {
                panic!("Unable tp setup params because of {}", e)
            })
    );
    
    let pass_hash = hash.hash_password(&password.into_bytes(), &salt).unwrap();
    
    pass_hash.to_string()
}

pub fn verify_password(hashed_pass: &String, raw_input: String) -> Result<(),Error> {
    let hash = Argon2::new(
        argon2::Algorithm::Argon2id,
        argon2::Version::V0x13,
        Params::new(15000, 2, 1, None)
            .unwrap_or_else(|e| {
                panic!("Unable tp setup params because of {}", e)
            })
    );
    
    let hashed_password =  PasswordHash::new(hashed_pass).expect("Cannot hash password");
    
    let bytes = raw_input.into_bytes();
    hash.verify_password(&bytes, &hashed_password)
}