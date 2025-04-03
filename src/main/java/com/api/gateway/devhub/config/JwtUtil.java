package com.api.gateway.devhub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.api.gateway.devhub.dto.TokenResponse;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

@Component
public class JwtUtil {

    @Value("${jwt.authUrl}")
    private String authUrl ;
    
    WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
            .baseUrl(authUrl)
            .build();
    }

    public Mono<TokenResponse> validateAndGetEmail(String token) {
    return webClient
        .post()
        .uri("/validateToken")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .retrieve()
        .bodyToMono(TokenResponse.class).onErrorResume(e -> {
            System.err.println("Token validation error: " + e.getMessage());
            return Mono.empty(); // Or return a default TokenResponse with null email
        });
}
}

