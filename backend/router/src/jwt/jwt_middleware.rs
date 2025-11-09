use std::future::{Ready, ready};

use crate::jwt::jwt_functions::verify_jwt;
use actix_web::{
    Error, HttpMessage, HttpResponse,
    body::{BoxBody, EitherBody, MessageBody},
    dev::{Service, ServiceRequest, ServiceResponse, Transform, forward_ready},
};
use futures_util::future::LocalBoxFuture;

/*
 * This is a middleware services running before sending particular request
 * to authenticated endpoints.
 */
pub struct JwtMiddleware;

pub struct JwtMiddlewareService<S> {
    service: S,
}

impl<S, B> Service<ServiceRequest> for JwtMiddlewareService<S>
where
    S: Service<ServiceRequest, Response = ServiceResponse<B>, Error = Error>,
    S::Future: 'static,
    B: MessageBody + 'static,
{
    type Response = ServiceResponse<EitherBody<B, BoxBody>>;
    type Error = Error;
    type Future = LocalBoxFuture<'static, Result<Self::Response, Self::Error>>;

    forward_ready!(service);

    /*
     * Check for the Authentication header. If it is off appropriate format
     * and is appropriately validated by the jwt methods it is put in the
     * request again as a direct jwt token
     */
    fn call(&self, req: ServiceRequest) -> Self::Future {
        let auth = req
            .headers()
            .get("authorization")
            .and_then(|h| h.to_str().ok());

        println!("Authorization token: {auth:?}");

        match auth {
            Some(header) => {
                if !header.starts_with("Bearer ") {
                    // let (req, _) = req.into_parts();
                    let res = HttpResponse::Unauthorized()
                        .json("Invalid token")
                        .map_into_right_body();
                    return Box::pin(async move { Ok(req.into_response(res)) });
                }

                let token = &header[7..];

                match verify_jwt(token.to_string()) {
                    Ok(token) => {
                        println!("{token:?}");
                        req.extensions_mut().insert(token.claims.groups);
                        req.extensions_mut().insert(token);
                        // println!("{}", token.claims.groups);
                        // println!("Raw token: {}", token);
                        // println!("Extracted token: {:?}", req.extensions().get::<String>());
                        let fut = self.service.call(req);
                        // println!("Successfully validated token");
                        Box::pin(async move {
                            let res = fut.await.unwrap();
                            Ok(res.map_into_left_body())
                        })
                    }
                    Err(_) => {
                        let res = HttpResponse::Unauthorized()
                            .json("Invalid token")
                            .map_into_right_body();
                        Box::pin(async move { Ok(req.into_response(res)) })
                    }
                }
            }
            None => {
                let res = HttpResponse::Unauthorized()
                    .json("Token Not Found in format Authorisation: Bearer <Token>")
                    .map_into_right_body();
                Box::pin(async move { Ok(req.into_response(res)) })
            }
        }
    }
}

impl<S, B> Transform<S, ServiceRequest> for JwtMiddleware
where
    S: Service<ServiceRequest, Response = ServiceResponse<B>, Error = Error>,
    S::Future: 'static,
    B: MessageBody + 'static,
{
    type Response = ServiceResponse<EitherBody<B, BoxBody>>;
    type Error = Error;
    type Transform = JwtMiddlewareService<S>;
    type InitError = ();
    type Future = Ready<Result<Self::Transform, Self::InitError>>;

    fn new_transform(&self, service: S) -> Self::Future {
        ready(Ok(JwtMiddlewareService { service }))
    }
}
