package com.example.demo.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import org.springframework.core.Ordered;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Component 
public class AuthFilter implements Ordered, GlobalFilter {
    
    @Override 
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Read the headers sent by the client (or k6 script)
        String orgId = exchange.getRequest().getHeaders().getFirst("X-Organization-Id");
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");

        // Simulate JWT Validation: Reject requests missing identity claims
        if (orgId == null || userId == null) {
            log.warn("Unauthorized request: Missing identity headers");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        log.info("Authenticated request for Org: {}, User: {}", orgId, userId);

        // The headers are valid, pass the request downstream to the core backend
        return chain.filter(exchange);
    }
    
    @Override 
    public int getOrder() {
        return -1;
    }
}