package com.booking.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    // Ensure this key matches the one in User Service!
    // Best practice: Load this from Config Server
    @Value("${jwt.secret-key}")
    private String secretKey;

    // Endpoints that do NOT require authentication
    @Value("${app.security.public-endpoints}")
    private List<String> publicEndpoints;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 1. Skip validation for public endpoints
        boolean isPublic = publicEndpoints.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (isPublic) {
            return chain.filter(exchange);
        }

        // 2. Check for Authorization header
        if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        try {
            // 3. Validate Token & Extract Claims
            Claims claims = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 4. Mutate Request: Add headers for downstream services
            // Downstream services will read these headers instead of parsing the JWT again
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Email", claims.getSubject()) // Send email/username
                    .header("X-User-Role", claims.get("role", String.class)) // Send role
                    .build();

            return chain.filter(exchange.mutate().request(request).build());

        } catch (Exception e) {
            // Token invalid or expired
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public int getOrder() {
        return -1; // High priority
    }
}