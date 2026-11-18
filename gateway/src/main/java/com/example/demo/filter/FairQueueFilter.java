
package com.example.demo.filter;

import com.example.demo.fair.QueuedTenantRequest;
import com.example.demo.fair.TenantQueueManager;
import com.example.demo.metrics.SchedulerMetrics;

import java.time.Instant;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

@Component
@ConditionalOnProperty(
    name = "scheduler.dwrr.enabled",
    havingValue = "true"
)
public class FairQueueFilter implements GlobalFilter, Ordered {

    private final TenantQueueManager queueManager;
    private final SchedulerMetrics schedulerMetrics;

    public FairQueueFilter(
            TenantQueueManager queueManager,
            SchedulerMetrics schedulerMetrics
    ) {
        this.queueManager = queueManager;
        this.schedulerMetrics = schedulerMetrics;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {

        long requestStart = System.nanoTime();

        String tenantId = exchange.getRequest()
                .getHeaders()
                .getFirst("X-ORGANIZATION-ID");

        String metricTenant = tenantId == null
                ? "unattributed"
                : tenantId;

        Mono<Void> requestFlow;

        if (tenantId == null) {

            requestFlow = chain.filter(exchange);

        } else {

            QueuedTenantRequest queueRequest =
                    new QueuedTenantRequest(
                            tenantId,
                            Instant.now(),
                            schedulerMetrics
                    );

            queueManager.enqueue(queueRequest);

            requestFlow = queueRequest.waitForAdmission()
                    .then(chain.filter(exchange));
        }

        return requestFlow.doFinally(signalType -> {

            long durationNanos =
                    System.nanoTime() - requestStart;

            schedulerMetrics.recordRequestDuration(
                    metricTenant,
                    durationNanos
            );

            schedulerMetrics.recordRequestTermination(
                    metricTenant,
                    signalType
            );
        });
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
