package com.example.demo.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Component 
public class AuthFilter implements Ordered, GlobalFilter {
    
    private static final String MOCK_ORG_ID = "00000000-0000-0000-0000-000000000001";
    private static final String MOCK_USER_ID = "00000000-0000-0000-0000-000000000002";

    @Override 
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) 
        {
            log.info("Incoming request {} {}" ,exchange.getRequest().getMethod(), exchange.getRequest().getURI());

            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
            .header("X-Organization;-Id", MOCK_ORG_ID)
            .header("X-User;-Id", MOCK_USER_ID)
            .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build())
            .then(Mono.fromRunnable(() -> log.info("Outgoing response {} {}" ,exchange.getResponse().getStatusCode(), exchange.getResponse().getStatusText())));
        }
    
    @Override 
    public int getOrder() {
        // sets priority as first here
        return -1;
    }
}
