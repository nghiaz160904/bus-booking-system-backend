package com.booking.bookingService.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 1. Read headers set by the API Gateway
        String userEmail = request.getHeader("X-User-Email");
        String userRole = request.getHeader("X-User-Role");

        // 2. If headers are present, create an Authentication object
        if (userEmail != null && userRole != null) {
            // Spring Security expects roles to be prefixed with "ROLE_" usually
            // but we can just add it here to be safe.
            String roleName = userRole.startsWith("ROLE_") ? userRole : "ROLE_" + userRole;
            
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);
            List<SimpleGrantedAuthority> authorities = Collections.singletonList(authority);

            // Create the authentication token (User is authenticated)
            UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(userEmail, null, authorities);

            // 3. Set the authentication in the context
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // 4. Continue the filter chain
        filterChain.doFilter(request, response);
    }
}