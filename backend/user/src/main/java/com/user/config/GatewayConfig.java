package com.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;


import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.*;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.*;

import com.user.Utility;

import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.*;

@Configuration
public class GatewayConfig {
    
    
    @Bean
    public RouterFunction<ServerResponse> routing() {
        // This needs to be added as the project grows 
        return route("tariff_route")
                .route(path("/tariff/**"), http())
                .before(uri(Utility.getEnvOrDotenv("TARIFF_URL")))
            .build(); 
    }
}