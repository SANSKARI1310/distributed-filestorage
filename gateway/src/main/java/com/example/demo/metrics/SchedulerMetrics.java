
package com.example.demo.metrics;

import com.example.demo.fair.TenantQueueManager;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class SchedulerMetrics {

    private final MeterRegistry registry;
    private final TenantQueueManager queueManager;

    public SchedulerMetrics(
            MeterRegistry registry,
            TenantQueueManager queueManager
    ) {
        this.registry = registry;
        this.queueManager = queueManager;
    }

    @PostConstruct
    public void registerMetrics() {
        for (String tenantId : new String[] {
                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                "cccccccc-cccc-cccc-cccc-cccccccccccc"
        }) {
            Gauge.builder(
                    "gateway.tenant.queue.depth",
                    queueManager,
                    manager -> manager.getQueueSize(tenantId)
            )
            .tag("tenant", tenantId)
            .description("Current queued requests for a tenant")
            .register(registry);
        }
    }
}

