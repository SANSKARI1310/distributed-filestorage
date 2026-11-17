package com.example.demo.filter;


import com.example.demo.fair.QueuedTenantRequest;
import com.example.demo.fair.TenantQueueManager;

import java.time.Instant;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@ConditionalOnProperty(
    name = "scheduler.dwrr.enabled",
    havingValue = "true"
)
public class FairQueueFilter implements GlobalFilter , Ordered {
 
    private final TenantQueueManager queueManager;

    FairQueueFilter(TenantQueueManager queueManager)
    {
        this.queueManager = queueManager;
    }

    public Mono<Void> filter(
        ServerWebExchange exchange,
        GatewayFilterChain chain
    )
    {
        String tenantId = exchange.getRequest()
            .getHeaders()
            .getFirst("X-ORGANIZATION-ID");

        if(tenantId == null)
        {
            return chain.filter(exchange);
        }

        QueuedTenantRequest queueRequest = new QueuedTenantRequest(tenantId , Instant.now());
        queueManager.enqueue(queueRequest);

        return queueRequest.waitForAdmission().then(chain.filter(exchange));
    }

    @Override
    public int getOrder()
    {
        return 0;
    }

}
