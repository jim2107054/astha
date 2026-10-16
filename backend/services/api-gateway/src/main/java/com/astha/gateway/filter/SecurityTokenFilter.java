package com.astha.gateway.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class SecurityTokenFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        // Public endpoints allowed through without token
        if (path.startsWith("/v1/auth/") ||
            path.startsWith("/v1/events") ||
            path.startsWith("/v1/providers/") ||
            path.startsWith("/actuator") ||
            path.startsWith("/v1/gateway/")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (!token.isBlank()) {
                // Token recognized - proceed downstream
                chain.doFilter(request, response);
                return;
            }
        }

        // Allow non-restricted reads, or pass through with anonymous principal
        chain.doFilter(request, response);
    }
}
