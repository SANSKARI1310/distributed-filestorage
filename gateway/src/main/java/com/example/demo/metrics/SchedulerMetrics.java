
package com.example.demo.metrics;

import com.example.demo.fair.TenantQueueManager;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;

import jakarta.annotation.PostConstruct;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import reactor.core.publisher.SignalType;
import org.springframework.stereotype.Component;

@Component
public class SchedulerMetrics {

    private final MeterRegistry registry;
    private final TenantQueueManager queueManager;
    private final Map<String, Timer> queueWaitTimers =
            new ConcurrentHashMap<>();

    private final Map<String, Counter> dispatchCounters =
            new ConcurrentHashMap<>();

    public SchedulerMetrics(
            MeterRegistry registry,
            TenantQueueManager queueManager
    ) {
        this.registry = registry;
        this.queueManager = queueManager;
    }

    private final Map<String, Counter> rejectedCounters =
        new ConcurrentHashMap<>();

    private final Map<String, Counter> droppedCounters =
        new ConcurrentHashMap<>();

    private final Map<String, Timer> requestTimers =
            new ConcurrentHashMap<>();

    private final Map<String, Counter> terminationCounters =
            new ConcurrentHashMap<>();

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
    public void recordQueueWait(String tenantId, long waitNanos) {

        getOrCreateQueueWaitTimer(tenantId)
                .record(waitNanos, TimeUnit.NANOSECONDS);
    }

    public void recordDispatch(String tenantId) {

        getOrCreateDispatchCounter(tenantId).increment();
    }

    private Timer getOrCreateQueueWaitTimer(String tenantId) {

        return queueWaitTimers.computeIfAbsent(
                tenantId,
                id -> Timer.builder("gateway.tenant.queue.wait")
                        .tag("tenant", id)
                        .description("Time spent waiting for scheduler admission")
                        .register(registry)
        );
    }

    private Counter getOrCreateDispatchCounter(String tenantId) {

        return dispatchCounters.computeIfAbsent(
                tenantId,
                id -> Counter.builder("gateway.tenant.dispatch.total")
                        .tag("tenant", id)
                        .description("Total requests admitted by the fair scheduler")
                        .register(registry)
        );
    }

    public void recordRejected(String tenantId) {
        getOrCreateRejectedCounter(tenantId).increment();
    }

    public void recordDropped(String tenantId) {
        getOrCreateDroppedCounter(tenantId).increment();
    }
    
    private Counter getOrCreateRejectedCounter(String tenantId)
    {
        return rejectedCounters.computeIfAbsent(tenantId , 
            id -> Counter.builder("gateway.tenant.requests.rejected")
                .tag("tenant", id)
                .description("Total requests rejected before process")
                .register(registry)
        );
    }

    private Counter getOrCreateDroppedCounter(String tenantId)
    {
        return droppedCounters.computeIfAbsent(tenantId , 
            id -> Counter.builder("gateway.tenant.requests.dropped")
                .tag("tenant", id)
                .description("Total requests dropped in process")
                .register(registry)
        );
    }

    public void recordRequestDuration(String tenantId, long durationNanos)
    {
        getOrCreateRecordTimer(tenantId).record(durationNanos , TimeUnit.NANOSECONDS);
    }

    private Timer getOrCreateRecordTimer(String tenantId)
    {
        return requestTimers.computeIfAbsent(tenantId, 
            id-> Timer.builder("gateway.tenant.request.duration")
            .tag("tenantId", id)
            .description("Complete gateway time cycle")
            .register(registry)
        );
    }

    public void recordRequestTermination(String tenantId , SignalType signalType)
    {
        String outcome =
            switch(signalType)
            {
                case ON_COMPLETE -> "completed";
                case ON_ERROR -> "error";
                case CANCEL -> "cancelled";
                default -> "other";
            };
        String key = tenantId + ":" + outcome;
        terminationCounters.computeIfAbsent(key
            ,ignored -> Counter.builder("gateway.requests.terminated")
            .tag("tenantId", tenantId)
            .tag("outcome" , outcome)
            .description("Requests terminated")
            .register(registry)
        ).increment();

    }
    
}

