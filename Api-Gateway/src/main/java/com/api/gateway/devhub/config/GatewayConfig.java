package com.api.gateway.devhub.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Autowired
    private AuthenticationFilter filter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/v1/**")
                        .filters(f -> f.filter(filter))
                        .uri("http://localhost:8080"))

                // .route("auth-service", r -> r.path("/auth/**")
                //         .filters(f -> f.filter(filter))
                //         .uri("lb://auth-service"))
                .build();
    }

}
