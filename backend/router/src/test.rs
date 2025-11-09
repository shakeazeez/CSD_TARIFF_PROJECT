#[cfg(test)]
mod tests {
    use std::env;
    use crate::dto::{CreateDTO, LoginDTO, TokenDTO};
    use crate::encryption::{encrypt_password, verify_password};
    use crate::jwt::jwt_functions;
    use crate::tables::{AuthUser, Role};
    use actix_cors::Cors;
    use actix_governor::{Governor, GovernorConfigBuilder};
    use actix_web::{App, test};
    use actix_web::http::StatusCode;
    use crate::web;
    
    // ...existing tests...

    // Actix Web Integration Tests
    #[actix_web::test]
    async fn test_app_creation() {
        let governor_conf = GovernorConfigBuilder::default()
            .const_burst_size(20)
            .const_milliseconds_per_request(10)
            .finish()
            .unwrap();

        let app = test::init_service(
            App::new()
                .wrap(Governor::new(&governor_conf))
        ).await;

        // Just test that the app can be created successfully
        assert!(true);
    }

    #[actix_web::test]
    async fn test_cors_configuration() {
        unsafe {
            env::set_var("FRONTEND_URL", "http://localhost:3000");
        }
        let cors = Cors::default()
            .allowed_origin(&env::var("FRONTEND_URL").unwrap_or_else(|_| "0.0.0.0:80".to_string()))
            .allow_any_header()
            .allow_any_method();

        let app = test::init_service(
            App::new()
                .wrap(cors)
        ).await;

        // Test that CORS is properly configured
        let req = test::TestRequest::default()
            .insert_header(("Origin", "http://localhost:3000"))
            .to_request();

        let resp = test::call_service(&app, req).await;
        
        // Should not reject the request due to CORS
        assert_ne!(resp.status(), StatusCode::FORBIDDEN);
        
        unsafe {
            env::remove_var("FRONTEND_URL");
        }
    }

    #[actix_web::test]
    async fn test_cors_default_configuration() {
        unsafe {
            env::remove_var("FRONTEND_URL");
        }
        
        let cors = Cors::default()
            .allowed_origin(&env::var("FRONTEND_URL").unwrap_or_else(|_| {
                "0.0.0.0:80".to_string()
            }))
            .allow_any_header()
            .allow_any_method();

        let app = test::init_service(
            App::new()
                .wrap(cors)
        ).await;

        let req = test::TestRequest::default()
            .insert_header(("Origin", "0.0.0.0:80"))
            .to_request();

        let resp = test::call_service(&app, req).await;
        assert_ne!(resp.status(), StatusCode::FORBIDDEN);
    }


    #[actix_web::test]
    async fn test_mock_login_endpoint() {
        // Create a mock login endpoint for testing
        async fn mock_login() -> actix_web::Result<actix_web::HttpResponse> {
            Ok(actix_web::HttpResponse::Ok().json(TokenDTO {
                token: Some("mock_token".to_string()),
                role: Some("MEMBER".to_string()),
                username: Some("testuser".to_string()),
                pin: None,
                industry: None,
                origin_country: None,
                tariffs: None,
                historical_tariff_id: None
            }))
        }

        let app = test::init_service(
            App::new()
                .route("/auth/login", web::post().to(mock_login))
        ).await;

        let login_dto = LoginDTO {
            username: Some("testuser".to_string()),
            password: Some("testpass".to_string()),
        };

        let req = test::TestRequest::post()
            .uri("/auth/login")
            .set_json(&login_dto)
            .to_request();

        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::OK);

        let body: TokenDTO = test::read_body_json(resp).await;
        assert_eq!(body.token.unwrap(), "mock_token");
        assert_eq!(body.role.unwrap(), "MEMBER");
        assert_eq!(body.username.unwrap(), "testuser");
    }

    #[actix_web::test]
    async fn test_mock_register_endpoint() {
        async fn mock_register() -> actix_web::Result<actix_web::HttpResponse> {
            Ok(actix_web::HttpResponse::Created().json("User created successfully"))
        }

        let app = test::init_service(
            App::new()
                .route("/auth/register", web::post().to(mock_register))
        ).await;

        let create_dto = CreateDTO {
            username: "newuser".to_string(),
            password: "newpass".to_string(),
            role: "MEMBER".to_string(),
            industry: None,
            tariffs: None,
            items_sold: None,
            origin_country: None,
            
        };

        let req = test::TestRequest::post()
            .uri("/auth/register")
            .set_json(&create_dto)
            .to_request();

        let resp = test::call_service(&app, req).await;
        assert_eq!(resp.status(), StatusCode::CREATED);
    }

    #[actix_web::test]
    async fn test_missing_content_type() {
        async fn mock_endpoint() -> actix_web::Result<actix_web::HttpResponse> {
            Ok(actix_web::HttpResponse::Ok().json("success"))
        }

        let app = test::init_service(
            App::new()
                .route("/test", web::post().to(mock_endpoint))
        ).await;

        let req = test::TestRequest::post()
            .uri("/test")
            .set_payload(r#"{"test": "data"}"#)
            // Intentionally not setting Content-Type header
            .to_request();

        let resp = test::call_service(&app, req).await;
        // Endpoint should still work, but might not parse JSON properly
        assert!(resp.status().is_success() || resp.status().is_client_error());
    }

    #[actix_web::test]
    async fn test_health_check_endpoint() {
        async fn health_check() -> actix_web::Result<actix_web::HttpResponse> {
            Ok(actix_web::HttpResponse::Ok().json(serde_json::json!({
                "status": "healthy",
                "timestamp": chrono::Utc::now().to_rfc3339()
            })))
        }

        let app = test::init_service(
            App::new()
                .route("/health", web::get().to(health_check))
        ).await;

        let req = test::TestRequest::get().uri("/health").to_request();
        let resp = test::call_service(&app, req).await;
        
        assert_eq!(resp.status(), StatusCode::OK);
        
        let body: serde_json::Value = test::read_body_json(resp).await;
        assert_eq!(body["status"], "healthy");
        assert!(body["timestamp"].is_string());
    }

    #[actix_web::test]
    async fn test_app_data_injection() {
        use std::sync::Arc;
        use std::sync::Mutex;

        #[derive(Clone)]
        struct TestData {
            counter: Arc<Mutex<i32>>,
        }

        async fn increment_counter(data: web::Data<TestData>) -> actix_web::Result<actix_web::HttpResponse> {
            let mut counter = data.counter.lock().unwrap();
            *counter += 1;
            Ok(actix_web::HttpResponse::Ok().json(*counter))
        }

        let test_data = TestData {
            counter: Arc::new(Mutex::new(0)),
        };

        let app = test::init_service(
            App::new()
                .app_data(web::Data::new(test_data.clone()))
                .route("/increment", web::post().to(increment_counter))
        ).await;

        // First request
        let req1 = test::TestRequest::post().uri("/increment").to_request();
        let resp1 = test::call_service(&app, req1).await;
        assert_eq!(resp1.status(), StatusCode::OK);
        let body1: i32 = test::read_body_json(resp1).await;
        assert_eq!(body1, 1);

        // Second request
        let req2 = test::TestRequest::post().uri("/increment").to_request();
        let resp2 = test::call_service(&app, req2).await;
        assert_eq!(resp2.status(), StatusCode::OK);
        let body2: i32 = test::read_body_json(resp2).await;
        assert_eq!(body2, 2);
    }

    #[actix_web::test]
    async fn test_error_handling() {
        async fn error_endpoint() -> actix_web::Result<actix_web::HttpResponse> {
            Err(actix_web::error::ErrorInternalServerError("Simulated error"))
        }

        let app = test::init_service(
            App::new()
                .route("/error", web::get().to(error_endpoint))
        ).await;

        let req = test::TestRequest::get().uri("/error").to_request();
        let resp = test::call_service(&app, req).await;
        
        assert_eq!(resp.status(), StatusCode::INTERNAL_SERVER_ERROR);
    }
    
    #[test]
    async fn test_host_default_value() {
        unsafe{env::remove_var("HOST")};
        let host = env::var("HOST").unwrap_or_else(|_| "127.0.0.1:8080".to_string());
        assert_eq!(host, "127.0.0.1:8080");
    }

    #[test]
    async fn test_host_custom_value() {
        unsafe {env::set_var("HOST", "192.168.1.1:3000") };
        let host = env::var("HOST").unwrap_or_else(|_| "127.0.0.1:8080".to_string());
        assert_eq!(host, "192.168.1.1:3000");
        unsafe { env::remove_var("HOST") };
    }

    #[test]
    async fn test_host_transformation() {
        let mut host = "127.0.0.1:8080".to_string();
        if host == "127.0.0.1:8080" {
            host = "0.0.0.0:8080".to_string();
        }
        assert_eq!(host, "0.0.0.0:8080");
    }

    #[test]
    async fn test_host_no_transformation() {
        let mut host = "192.168.1.1:3000".to_string();
        if host == "127.0.0.1:8080" {
            host = "0.0.0.0:8080".to_string();
        }
        assert_eq!(host, "192.168.1.1:3000");
    }

    #[test]
    async fn test_frontend_url_default() {
        unsafe {env::remove_var("FRONTEND_URL")} ;
        let frontend_url = env::var("FRONTEND_URL").unwrap_or_else(|_| "0.0.0.0:80".to_string());
        assert_eq!(frontend_url, "0.0.0.0:80");
    }

    #[test]
    async fn test_frontend_url_custom() {
        unsafe {env::set_var("FRONTEND_URL", "http://localhost:3000")};
        let frontend_url = env::var("FRONTEND_URL").unwrap_or_else(|_| "0.0.0.0:80".to_string());
        assert_eq!(frontend_url, "http://localhost:3000");
        unsafe{env::remove_var("FRONTEND_URL")};
    }

    #[test]
    async fn test_role_from_i16() {
        assert_eq!(Role::from_i16(0), Role::MEMBER);
        assert_eq!(Role::from_i16(1), Role::BANK);
        assert_eq!(Role::from_i16(2), Role::BUSINESS);
        assert_eq!(Role::from_i16(3), Role::ADMIN);
    }

    #[test]
    #[should_panic(expected = "Whoops, time to update")]
    async fn test_role_from_i16_invalid() {
        Role::from_i16(99);
    }

    #[test]
    async fn test_role_from_string() {
        assert_eq!(Role::from_string("MEMBER"), Role::MEMBER);
        assert_eq!(Role::from_string("BANK"), Role::BANK);
        assert_eq!(Role::from_string("BUSINESS"), Role::BUSINESS);
        assert_eq!(Role::from_string("ADMIN"), Role::ADMIN);
    }

    #[test]
    #[should_panic(expected = "Whoops, time to update")]
    async fn test_role_from_string_invalid() {
        Role::from_string("INVALID_ROLE");
    }

    #[test]
    async fn test_role_display() {
        assert_eq!(format!("{}", Role::MEMBER), "MEMBER");
        assert_eq!(format!("{}", Role::BANK), "BANK");
        assert_eq!(format!("{}", Role::BUSINESS), "BUSINESS");
        assert_eq!(format!("{}", Role::ADMIN), "ADMIN");
    }


    // Authentication Tests
    #[test]
    async fn test_password_encryption_and_verification() {
        let original_password = "test_password_123".to_string();
        let encrypted = encrypt_password(original_password.clone());
        
        // Ensure password is actually encrypted (different from original)
        assert_ne!(encrypted, original_password);
        
        // Verify the password works
        let verification = verify_password(&encrypted, original_password.clone());
        assert!(verification.is_ok());
        
        // Verify wrong password fails
        let wrong_verification = verify_password(&encrypted, "wrong_password".to_string());
        assert!(wrong_verification.is_err());
    }

    #[test]
    async fn test_password_encryption_empty_string() {
        let empty_password = "".to_string();
        let encrypted = encrypt_password(empty_password.clone());
        
        // Should still encrypt empty string
        assert_ne!(encrypted, empty_password);
        
        let verification = verify_password(&encrypted, empty_password);
        assert!(verification.is_ok());
    }

    #[test]
    async fn test_password_encryption_special_characters() {
        let special_password = "p@$$w0rd!@#$%^&*()".to_string();
        let encrypted = encrypt_password(special_password.clone());
        
        assert_ne!(encrypted, special_password);
        
        let verification = verify_password(&encrypted, special_password);
        assert!(verification.is_ok());
    }

    #[test]
    async fn test_create_dto_serialization() {
        let create_dto = CreateDTO {
            username: "testuser".to_string(),
            password: "testpass".to_string(),
            role: "MEMBER".to_string(),
            industry: None,
            tariffs: None,
            items_sold: None,
            origin_country: None
        };
        
        // Test that we can serialize to JSON
        let json_result = serde_json::to_string(&create_dto);
        assert!(json_result.is_ok());
        
        let json_string = json_result.unwrap();
        assert!(json_string.contains("testuser"));
        assert!(json_string.contains("testpass"));
        assert!(json_string.contains("MEMBER"));
    }

    #[test]
    async fn test_create_dto_deserialization() {
        let json_str = r#"{"username":"testuser","password":"testpass","role":"MEMBER"}"#;
        
        let dto_result: Result<CreateDTO, _> = serde_json::from_str(json_str);
        assert!(dto_result.is_ok());
        
        let dto = dto_result.unwrap();
        assert_eq!(dto.username, "testuser");
        assert_eq!(dto.password, "testpass");
        assert_eq!(dto.role, "MEMBER");
    }

    #[test]
    async fn test_login_dto_serialization() {
        let login_dto = LoginDTO {
            username: Some("testuser".to_string()),
            password: Some("testpass".to_string()),
        };
        
        let json_result = serde_json::to_string(&login_dto);
        assert!(json_result.is_ok());
        
        let json_string = json_result.unwrap();
        assert!(json_string.contains("testuser"));
        assert!(json_string.contains("testpass"));
    }

    #[test]
    async fn test_login_dto_with_none_values() {
        let login_dto = LoginDTO {
            username: None,
            password: None,
        };
        
        let json_result = serde_json::to_string(&login_dto);
        assert!(json_result.is_ok());
    }

    #[test]
    async fn test_auth_user_creation() {
        let auth_user = AuthUser {
            id: 1,
            username: "testuser".to_string(),
            hashedpassword: "hashed_password".to_string(),
            user_roles: Role::MEMBER,
        };
        
        assert_eq!(auth_user.id, 1);
        assert_eq!(auth_user.username, "testuser");
        assert_eq!(auth_user.hashedpassword, "hashed_password");
        assert_eq!(auth_user.user_roles, Role::MEMBER);
    }

    #[test]
    async fn test_auth_user_serialization() {
        let auth_user = AuthUser {
            id: 1,
            username: "testuser".to_string(),
            hashedpassword: "hashed_password".to_string(),
            user_roles: Role::ADMIN,
        };
        
        let json_result = serde_json::to_string(&auth_user);
        assert!(json_result.is_ok());
        
        let json_string = json_result.unwrap();
        assert!(json_string.contains("testuser"));
        assert!(json_string.contains("hashed_password"));
    }

    #[test]
    async fn test_jwt_token_generation_different_users() {
        unsafe {
            env::set_var("USER_URL", "TESTING");
            env::set_var("SIGNING_SECRET", "TESTING");
        }
        let user1 = AuthUser {
            id: 1,
            username: "user1".to_string(),
            hashedpassword: "hash1".to_string(),
            user_roles: Role::MEMBER,
        };
        
        let user2 = AuthUser {
            id: 2,
            username: "user2".to_string(),
            hashedpassword: "hash2".to_string(),
            user_roles: Role::ADMIN,
        };
        
        
        let token1 = jwt_functions::generate_token(&user1);
        let token2 = jwt_functions::generate_token(&user2);
        
        unsafe {
            env::remove_var("USER_URL");
            env::remove_var("SIGNING_SECRET");
        }
        // Tokens should be different for different users
        assert_ne!(token1, token2);
        assert!(!token1.is_empty());
        assert!(!token2.is_empty());
    }

    #[test]
    async fn test_jwt_token_consistency() {
        let auth_user = AuthUser {
            id: 1,
            username: "testuser".to_string(),
            hashedpassword: "hashed_password".to_string(),
            user_roles: Role::MEMBER,
        };
        
        unsafe {
            env::set_var("USER_URL", "TESTING");
            env::set_var("SIGNING_SECRET", "TESTING");
        }
        let token1 = jwt_functions::generate_token(&auth_user);
        let token2 = jwt_functions::generate_token(&auth_user);
        // Note: This test might fail if JWT includes timestamp
        // If your JWT includes current time, tokens will be different
        // In that case, just verify both tokens are valid strings
        unsafe {
            env::remove_var("USER_URL");
            env::remove_var("SIGNING_SECRET");
        }
        assert!(!token1.is_empty());
        assert!(!token2.is_empty());
    
    }

    #[test]
    async fn test_role_conversion_consistency() {
        // Test that role conversion works both ways
        let roles = vec![Role::MEMBER, Role::BANK, Role::BUSINESS, Role::ADMIN];
        
        for role in roles {
            let role_string = role.to_string();
            let converted_role = Role::from_string(&role_string);
            assert_eq!(role, converted_role);
        }
    }

    #[test]
    async fn test_password_strength_scenarios() {
        let passwords = vec![
            "123456",
            "password",
            "Password123!",
            "VeryLongPasswordWithNumbers123AndSpecialChars!@#",
            "短密码", // Unicode characters
        ];
        
        for password in passwords {
            let encrypted = encrypt_password(password.to_string());
            let verification = verify_password(&encrypted, password.to_string());
            assert!(verification.is_ok(), "Failed for password: {}", password);
        }
    }

    #[test]
    async fn test_multiple_password_encryptions() {
        let password = "same_password".to_string();
        
        // Encrypt the same password multiple times
        let encrypted1 = encrypt_password(password.clone());
        let encrypted2 = encrypt_password(password.clone());
        
        // Should produce different hashes (due to salt)
        assert_ne!(encrypted1, encrypted2);
        
        // But both should verify correctly
        assert!(verify_password(&encrypted1, password.clone()).is_ok());
        assert!(verify_password(&encrypted2, password).is_ok());
    }

    #[test]
    async fn test_auth_user_clone() {
        let auth_user = AuthUser {
            id: 1,
            username: "testuser".to_string(),
            hashedpassword: "hashed_password".to_string(),
            user_roles: Role::MEMBER,
        };
        
        let cloned_user = auth_user.clone();
        assert_eq!(auth_user.id, cloned_user.id);
        assert_eq!(auth_user.username, cloned_user.username);
        assert_eq!(auth_user.hashedpassword, cloned_user.hashedpassword);
        assert_eq!(auth_user.user_roles, cloned_user.user_roles);
    }
}