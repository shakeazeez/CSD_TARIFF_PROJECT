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
        String tariffUrl = Utility.getEnvOrDotenv("TARIFF_URL");
        if (tariffUrl == null || tariffUrl.isEmpty()) {
            tariffUrl = "http://localhost:8081"; // default for tests
        }
        
        String newsUrl = Utility.getEnvOrDotenv("NEWS_URL");
        if (newsUrl == null || newsUrl.isEmpty()) {
            newsUrl = "http://localhost:8083"; // default for news service
        }
        
        return route("tariff_route")
                .route(path("/tariff/**"), http())
                .before(uri(tariffUrl))
            .build()
            .and(route("news_route")
                .route(path("/news/**"), http())
                .before(uri(newsUrl))
            .build()); 
    }
}