package com.api.gateway.devhub.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@RefreshScope
@Component
public class AuthenticationFilter implements GatewayFilter {
    private final RouterValidator routerValidator;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthenticationFilter(RouterValidator routerValidator, JwtUtil jwtUtil) {
        this.routerValidator = routerValidator;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        
        System.out.println("Processing request: " + path);  // Simple debug

        if (routerValidator.isSecured.test(request)) {
            System.out.println("Secured endpoint detected: " + path);  // Simple debug
            
            if (this.isAuthMissing(request)) {
                System.out.println("Authorization header missing");  // Simple debug
                return this.onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String token = extractToken(authHeader);

            if (token == null) {
                System.out.println("Invalid token format");  // Simple debug
                return this.onError(exchange, HttpStatus.UNAUTHORIZED);
            }
            
            System.out.println("About to validate token");  // Simple debug
            
            // This is the key change - we need to subscribe to the Mono by returning it
            return jwtUtil.validateAndGetEmail(token)
                .flatMap(tokenResponse -> {
                    System.out.println("Token validated: " + (tokenResponse != null));  // Simple debug
                    
                    if (tokenResponse == null || tokenResponse.getEmail() == null) {
                        System.out.println("Token validation returned null or empty response");  // Simple debug
                        return this.onError(exchange, HttpStatus.UNAUTHORIZED);
                    }
                    
                    System.out.println("Token validated for user: " + tokenResponse.getEmail());  // Simple debug
                    
                    // Create a new exchange with the email header
                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                            .header("email", tokenResponse.getEmail())
                            .build();
                    
                    ServerWebExchange modifiedExchange = exchange.mutate()
                            .request(modifiedRequest)
                            .build();
                    
                    // Continue the filter chain with the modified exchange
                    return chain.filter(modifiedExchange);
                })
                .onErrorResume(error -> {
                    System.out.println("Error during token validation: " + error.getMessage());  // Simple debug
                    return this.onError(exchange, HttpStatus.UNAUTHORIZED);
                });
        }
        
        System.out.println("Non-secured endpoint, proceeding");  // Simple debug
        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    private boolean isAuthMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
    }

    private String extractToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7).trim();
        }
        return null;
    }
}