package com.api.gateway.devhub.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {

    public static final List<String> openApiEndpoints = List.of(
            "/api/v1/auth/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public Predicate<ServerHttpRequest> isSecured = request -> {
        String path = request.getURI().getPath();
        return openApiEndpoints.stream()
                .noneMatch(pattern -> pathMatcher.match(pattern, path));
    };
}
